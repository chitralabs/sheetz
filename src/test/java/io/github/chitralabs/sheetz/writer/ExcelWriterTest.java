package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.Format;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ExcelWriterTest {

    private SheetzConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = SheetzConfig.defaults();
    }

    // -------- model --------

    public static class Product {
        public String name;
        public Double price;
        public Boolean inStock;

        public Product() {}

        public Product(String name, Double price, Boolean inStock) {
            this.name = name;
            this.price = price;
            this.inStock = inStock;
        }
    }

    // -------- helper --------

    private Workbook openXlsx(Path file) throws IOException {
        return new XSSFWorkbook(new FileInputStream(file.toFile()));
    }

    // -------- write to Path --------

    @Test
    void writeXlsx_createsValidWorkbook() throws IOException {
        Path file = tempDir.resolve("out.xlsx");
        List<Product> data = List.of(
                new Product("Widget", 9.99, true),
                new Product("Gadget", 24.50, false));

        new ExcelWriter<>(Product.class, config).write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2); // header + 2 data
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("name");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Widget");
            assertThat(sheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(9.99);
        }
    }

    // -------- custom sheet name --------

    @Test
    void writeWithCustomSheetName() throws IOException {
        Path file = tempDir.resolve("named.xlsx");
        List<Product> data = List.of(new Product("A", 1.0, true));

        new ExcelWriter<>(Product.class, config).sheetName("Inventory").write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            assertThat(wb.getSheetName(0)).isEqualTo("Inventory");
        }
    }

    // -------- freeze header --------

    @Test
    void writeWithFreezeHeader() throws IOException {
        Path file = tempDir.resolve("frozen.xlsx");
        List<Product> data = List.of(new Product("A", 1.0, true));

        new ExcelWriter<>(Product.class, config).freezeHeader(true).write(data, file, Format.XLSX);

        // Just verify it doesn't throw — POI doesn't expose freeze pane info easily
        assertThat(file).exists();
    }

    // -------- streaming mode --------

    @Test
    void writeInStreamingMode_succeeds() throws IOException {
        Path file = tempDir.resolve("stream.xlsx");
        List<Product> data = List.of(new Product("A", 1.0, true));

        new ExcelWriter<>(Product.class, config).streaming(true).write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(1);
        }
    }

    // -------- write to OutputStream --------

    @Test
    void writeToOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Product> data = List.of(new Product("Bolt", 0.50, true));

        new ExcelWriter<>(Product.class, config).write(data, baos, Format.XLSX);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()))) {
            assertThat(wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).isEqualTo("Bolt");
        }
    }

    // -------- empty data --------

    @Test
    void writeEmptyList_throws() {
        Path file = tempDir.resolve("fail.xlsx");

        assertThatThrownBy(() -> new ExcelWriter<>(Product.class, config).write(List.of(), file, Format.XLSX))
                .isInstanceOf(SheetzException.class);
    }

    // -------- column width --------

    public static class Wide {
        @Column(width = 30)
        public String description;

        public Wide() {}

        public Wide(String description) {
            this.description = description;
        }
    }

    @Test
    void writeWithColumnWidth_setsWidth() throws IOException {
        Path file = tempDir.resolve("wide.xlsx");
        List<Wide> data = List.of(new Wide("Long text goes here"));

        new ExcelWriter<>(Wide.class, config).write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            int widthInUnits = wb.getSheetAt(0).getColumnWidth(0);
            // 30 characters * 256 units = 7680
            assertThat(widthInUnits).isEqualTo(30 * 256);
        }
    }

    // -------- null value handling --------

    @Test
    void writeWithNullFields_writesBlankCells() throws IOException {
        Path file = tempDir.resolve("nulls.xlsx");
        List<Product> data = List.of(new Product(null, null, null));

        new ExcelWriter<>(Product.class, config).write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Row row = wb.getSheetAt(0).getRow(1);
            assertThat(row.getCell(0).getCellType()).isEqualTo(CellType.BLANK);
        }
    }

    // -------- date handling --------

    public static class Dated {
        public String name;
        public LocalDate created;

        public Dated() {}

        public Dated(String name, LocalDate created) {
            this.name = name;
            this.created = created;
        }
    }

    @Test
    void writeWithLocalDate_appliesDateStyle() throws IOException {
        Path file = tempDir.resolve("dates.xlsx");
        List<Dated> data = List.of(new Dated("Item", LocalDate.of(2025, 1, 15)));

        new ExcelWriter<>(Dated.class, config).write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Cell cell = wb.getSheetAt(0).getRow(1).getCell(1);
            // Date cells are stored as numeric in Excel
            assertThat(cell.getCellType()).isEqualTo(CellType.NUMERIC);
        }
    }
}
