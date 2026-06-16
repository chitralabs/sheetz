package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MergedCellReadTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Sheetz.reset();
    }

    public static class MergedRow {
        @Column("Category")
        public String category;

        @Column("Item")
        public String item;

        @Column("Price")
        public Double price;

        public MergedRow() {}
    }

    @Test
    void readMergedCells_resolvesToMasterValue() throws IOException {
        // Create a workbook with merged cells in Category column
        Path file = tempDir.resolve("merged.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Category");
            header.createCell(1).setCellValue("Item");
            header.createCell(2).setCellValue("Price");

            // Data rows - Category "Electronics" spans rows 1-2
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Electronics");
            r1.createCell(1).setCellValue("Phone");
            r1.createCell(2).setCellValue(999.0);

            Row r2 = sheet.createRow(2);
            // Cell 0 is blank (merged with above)
            r2.createCell(0); // blank cell
            r2.createCell(1).setCellValue("Laptop");
            r2.createCell(2).setCellValue(1499.0);

            // Merge category cells
            sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        List<MergedRow> rows = Sheetz.read(file, MergedRow.class);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).category).isEqualTo("Electronics");
        assertThat(rows.get(0).item).isEqualTo("Phone");
        assertThat(rows.get(1).category).isEqualTo("Electronics"); // Resolved from master
        assertThat(rows.get(1).item).isEqualTo("Laptop");
    }
}
