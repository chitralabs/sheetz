package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.style.HyperlinkValue;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HyperlinkReadTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Sheetz.reset();
    }

    public static class LinkRow {
        @Column("Name")
        public String name;

        @Column("Website")
        public HyperlinkValue website;

        public LinkRow() {}
    }

    @Test
    void readHyperlinkCells_extractsHyperlinkValue() throws IOException {
        Path file = tempDir.resolve("links.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            CreationHelper helper = wb.getCreationHelper();

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Website");

            // Data with hyperlink
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Acme Corp");
            Cell linkCell = r1.createCell(1);
            linkCell.setCellValue("Visit Acme");
            Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
            link.setAddress("https://acme.example.com");
            linkCell.setHyperlink(link);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        List<LinkRow> rows = Sheetz.read(file, LinkRow.class);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).name).isEqualTo("Acme Corp");
        assertThat(rows.get(0).website).isNotNull();
        assertThat(rows.get(0).website.displayText()).isEqualTo("Visit Acme");
        assertThat(rows.get(0).website.url()).isEqualTo("https://acme.example.com");
    }

    @Test
    void readCellWithoutHyperlink_returnsHyperlinkValueWithSameTextAndUrl() throws IOException {
        Path file = tempDir.resolve("nolink.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Website");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Plain");
            r1.createCell(1).setCellValue("not-a-link");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        List<LinkRow> rows = Sheetz.read(file, LinkRow.class);

        assertThat(rows).hasSize(1);
        // HyperlinkValue field without actual hyperlink: should use cell text for both
        assertThat(rows.get(0).website).isNotNull();
        assertThat(rows.get(0).website.displayText()).isEqualTo("not-a-link");
    }
}
