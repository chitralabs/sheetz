package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.Format;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.ValidationResult;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ExcelReaderTest {

    private SheetzConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = SheetzConfig.defaults();
    }

    // -------- simple model --------

    public static class Product {
        public String name;
        public Double price;

        public Product() {}
    }

    // -------- helper to create XLSX --------

    private Path createXlsx(String filename, String[] headers, Object[][] rows) throws IOException {
        Path file = tempDir.resolve(filename);
        try (Workbook wb = new XSSFWorkbook(); OutputStream os = new FileOutputStream(file.toFile())) {
            Sheet sheet = wb.createSheet("Data");
            Row hdr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                hdr.createCell(i).setCellValue(headers[i]);
            }
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < rows[r].length; c++) {
                    Object v = rows[r][c];
                    if (v instanceof String) row.createCell(c).setCellValue((String) v);
                    else if (v instanceof Number) row.createCell(c).setCellValue(((Number) v).doubleValue());
                    else if (v == null) row.createCell(c).setBlank();
                }
            }
            wb.write(os);
        }
        return file;
    }

    // -------- read from Path --------

    @Test
    void readXlsx_mapsColumnsToFields() throws IOException {
        Path file = createXlsx("products.xlsx",
                new String[]{"name", "price"},
                new Object[][]{{"Widget", 9.99}, {"Gadget", 24.50}});

        List<Product> products = new ExcelReader<>(Product.class, config).read(file);

        assertThat(products).hasSize(2);
        assertThat(products.get(0).name).isEqualTo("Widget");
        assertThat(products.get(0).price).isEqualTo(9.99);
        assertThat(products.get(1).name).isEqualTo("Gadget");
    }

    @Test
    void readXlsx_emptySheet_returnsEmptyList() throws IOException {
        Path file = createXlsx("empty.xlsx", new String[]{"name", "price"}, new Object[][]{});

        List<Product> products = new ExcelReader<>(Product.class, config).read(file);

        assertThat(products).isEmpty();
    }

    // -------- read from InputStream --------

    @Test
    void readFromInputStream() throws IOException {
        Path file = createXlsx("stream.xlsx",
                new String[]{"name", "price"},
                new Object[][]{{"Bolt", 0.50}});

        try (InputStream is = new FileInputStream(file.toFile())) {
            List<Product> products = new ExcelReader<>(Product.class, config).read(is, Format.XLSX);
            assertThat(products).hasSize(1);
            assertThat(products.get(0).name).isEqualTo("Bolt");
        }
    }

    // -------- sheet selection --------

    @Test
    void readBySheetName() throws IOException {
        Path file = tempDir.resolve("multi.xlsx");
        try (Workbook wb = new XSSFWorkbook(); OutputStream os = new FileOutputStream(file.toFile())) {
            Sheet s1 = wb.createSheet("Ignore");
            s1.createRow(0).createCell(0).setCellValue("name");

            Sheet s2 = wb.createSheet("Target");
            Row hdr = s2.createRow(0);
            hdr.createCell(0).setCellValue("name");
            hdr.createCell(1).setCellValue("price");
            Row data = s2.createRow(1);
            data.createCell(0).setCellValue("Found");
            data.createCell(1).setCellValue(42.0);

            wb.write(os);
        }

        List<Product> products = new ExcelReader<>(Product.class, config).sheet("Target").read(file);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).name).isEqualTo("Found");
    }

    // -------- readMaps --------

    @Test
    void readMaps_returnsHeaderKeyedMaps() throws IOException {
        Path file = createXlsx("maps.xlsx",
                new String[]{"col1", "col2"},
                new Object[][]{{"A", "B"}});

        List<Map<String, Object>> maps = ExcelReader.readMaps(file, config);

        assertThat(maps).hasSize(1);
        assertThat(maps.get(0)).containsEntry("col1", "A");
    }

    // -------- readRaw --------

    @Test
    void readRaw_returnsStringArraysIncludingHeader() throws IOException {
        Path file = createXlsx("raw.xlsx",
                new String[]{"a", "b"},
                new Object[][]{{"1", "2"}});

        List<String[]> rows = ExcelReader.readRaw(file, config);

        assertThat(rows).hasSizeGreaterThanOrEqualTo(2);
        assertThat(rows.get(0)).containsExactly("a", "b");
    }

    // -------- validation --------

    public static class Required {
        @Column(required = true)
        public String sku;

        public String name;

        public Required() {}
    }

    @Test
    void validate_collectsErrors() throws IOException {
        Path file = createXlsx("validate.xlsx",
                new String[]{"sku", "name"},
                new Object[][]{{null, "NoSku"}, {"S001", "HasSku"}});

        ValidationResult<Required> result = new ExcelReader<>(Required.class, config).validate(file);

        assertThat(result.validCount()).isEqualTo(1);
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.successRate()).isGreaterThan(0).isLessThan(100);
    }

    // -------- XLS format --------

    @Test
    void readXls_worksWithLegacyFormat() throws IOException {
        Path file = tempDir.resolve("legacy.xls");
        try (Workbook wb = new HSSFWorkbook(); OutputStream os = new FileOutputStream(file.toFile())) {
            Sheet sheet = wb.createSheet("Data");
            Row hdr = sheet.createRow(0);
            hdr.createCell(0).setCellValue("name");
            hdr.createCell(1).setCellValue("price");
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("OldFormat");
            data.createCell(1).setCellValue(7.77);
            wb.write(os);
        }

        List<Product> products = new ExcelReader<>(Product.class, config).read(file);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).name).isEqualTo("OldFormat");
    }

    // -------- error handling --------

    @Test
    void readNonExistentFile_throwsSheetzException() {
        Path missing = tempDir.resolve("nope.xlsx");

        assertThatThrownBy(() -> new ExcelReader<>(Product.class, config).read(missing))
                .isInstanceOf(SheetzException.class);
    }
}
