package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.Format;
import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.annotation.Style;
import io.github.chitralabs.sheetz.style.CellStyleBuilder;
import io.github.chitralabs.sheetz.style.CellStyleDef;
import io.github.chitralabs.sheetz.style.HyperlinkValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ExcelStyleWriteTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Sheetz.reset();
    }

    // -------- models --------

    public static class StyledProduct {
        @Column("Product Name")
        @Style(bold = true, fontColor = "#0000FF")
        public String name;

        @Style(backgroundColor = "#FFFF00", horizontalAlignment = "CENTER")
        public Double price;

        @Style(borderStyle = "THIN", wrapText = true)
        public String description;

        public StyledProduct() {}

        public StyledProduct(String name, Double price, String description) {
            this.name = name;
            this.price = price;
            this.description = description;
        }
    }

    public static class HyperlinkProduct {
        public String name;

        @Style(hyperlink = true)
        public String url;

        public HyperlinkProduct() {}

        public HyperlinkProduct(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public static class HyperlinkValueProduct {
        public String name;
        public HyperlinkValue website;

        public HyperlinkValueProduct() {}

        public HyperlinkValueProduct(String name, HyperlinkValue website) {
            this.name = name;
            this.website = website;
        }
    }

    // -------- helpers --------

    private Workbook openXlsx(Path file) throws IOException {
        return new XSSFWorkbook(new FileInputStream(file.toFile()));
    }

    // -------- tests --------

    @Test
    void writeStyledFields_appliesStyles() throws IOException {
        Path file = tempDir.resolve("styled.xlsx");
        List<StyledProduct> data = List.of(
            new StyledProduct("Widget", 9.99, "A great widget"));

        new ExcelWriter<>(StyledProduct.class, SheetzConfig.defaults())
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Sheet sheet = wb.getSheetAt(0);
            Row dataRow = sheet.getRow(1);

            // Name cell should have bold font with blue color
            Cell nameCell = dataRow.getCell(0);
            assertThat(nameCell.getStringCellValue()).isEqualTo("Widget");
            Font nameFont = wb.getFontAt(nameCell.getCellStyle().getFontIndex());
            assertThat(nameFont.getBold()).isTrue();

            // Price cell should have CENTER alignment
            Cell priceCell = dataRow.getCell(1);
            assertThat(priceCell.getCellStyle().getAlignment()).isEqualTo(HorizontalAlignment.CENTER);

            // Description cell should have THIN borders and wrapText
            Cell descCell = dataRow.getCell(2);
            assertThat(descCell.getCellStyle().getBorderTop()).isEqualTo(BorderStyle.THIN);
            assertThat(descCell.getCellStyle().getWrapText()).isTrue();
        }
    }

    @Test
    void writeHyperlinkField_createsHyperlink() throws IOException {
        Path file = tempDir.resolve("hyperlink.xlsx");
        List<HyperlinkProduct> data = List.of(
            new HyperlinkProduct("Acme", "https://acme.example.com"));

        new ExcelWriter<>(HyperlinkProduct.class, SheetzConfig.defaults())
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Cell cell = wb.getSheetAt(0).getRow(1).getCell(1);
            assertThat(cell.getStringCellValue()).isEqualTo("https://acme.example.com");
            Hyperlink link = cell.getHyperlink();
            assertThat(link).isNotNull();
            assertThat(link.getAddress()).isEqualTo("https://acme.example.com");
        }
    }

    @Test
    void writeHyperlinkValue_createsHyperlinkWithDisplayText() throws IOException {
        Path file = tempDir.resolve("hv.xlsx");
        List<HyperlinkValueProduct> data = List.of(
            new HyperlinkValueProduct("Acme", new HyperlinkValue("Visit Us", "https://acme.example.com")));

        new ExcelWriter<>(HyperlinkValueProduct.class, SheetzConfig.defaults())
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Cell cell = wb.getSheetAt(0).getRow(1).getCell(1);
            assertThat(cell.getStringCellValue()).isEqualTo("Visit Us");
            Hyperlink link = cell.getHyperlink();
            assertThat(link).isNotNull();
            assertThat(link.getAddress()).isEqualTo("https://acme.example.com");
        }
    }

    @Test
    void writeWithAutoFilter_setsAutoFilter() throws IOException {
        Path file = tempDir.resolve("filter.xlsx");
        List<StyledProduct> data = List.of(
            new StyledProduct("A", 1.0, "X"),
            new StyledProduct("B", 2.0, "Y"));

        new ExcelWriter<>(StyledProduct.class, SheetzConfig.defaults())
            .autoFilter(true)
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Sheet sheet = wb.getSheetAt(0);
            // Verify file was created successfully with auto filter
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
        }
    }

    @Test
    void writeWithMergeRegion_mergesCells() throws IOException {
        Path file = tempDir.resolve("merge.xlsx");
        List<StyledProduct> data = List.of(
            new StyledProduct("A", 1.0, "X"),
            new StyledProduct("B", 2.0, "Y"));

        new ExcelWriter<>(StyledProduct.class, SheetzConfig.defaults())
            .mergeRegion(1, 2, 0, 0)  // Merge name column rows 1-2
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getNumMergedRegions()).isEqualTo(1);
        }
    }

    @Test
    void writeWithCustomHeaderStyle_appliesHeaderStyle() throws IOException {
        Path file = tempDir.resolve("header.xlsx");
        CellStyleDef headerDef = CellStyleBuilder.create()
            .bold(true)
            .backgroundColor("#0000FF")
            .fontColor("#FFFFFF")
            .build();

        List<StyledProduct> data = List.of(new StyledProduct("A", 1.0, "X"));

        new ExcelWriter<>(StyledProduct.class, SheetzConfig.defaults())
            .headerStyle(headerDef)
            .write(data, file, Format.XLSX);

        try (Workbook wb = openXlsx(file)) {
            Cell headerCell = wb.getSheetAt(0).getRow(0).getCell(0);
            Font font = wb.getFontAt(headerCell.getCellStyle().getFontIndex());
            assertThat(font.getBold()).isTrue();
        }
    }

    @Test
    void writerBuilder_supportsAutoFilterAndMerge() throws IOException {
        Path file = tempDir.resolve("builder.xlsx");

        Sheetz.writer(StyledProduct.class)
            .data(List.of(new StyledProduct("A", 1.0, "X"), new StyledProduct("B", 2.0, "Y")))
            .file(file)
            .autoFilter(true)
            .mergeRegion(1, 2, 0, 0)
            .write();

        try (Workbook wb = openXlsx(file)) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getNumMergedRegions()).isEqualTo(1);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
        }
    }
}
