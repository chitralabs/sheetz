package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class StreamingReaderTest {

    private SheetzConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = SheetzConfig.defaults();
    }

    // -------- model --------

    public static class Item {
        public String name;
        public Double price;

        public Item() {}
    }

    // -------- helpers --------

    private Path createXlsx(int dataRows) throws IOException {
        Path file = tempDir.resolve("stream.xlsx");
        try (Workbook wb = new XSSFWorkbook(); OutputStream os = new FileOutputStream(file.toFile())) {
            Sheet sheet = wb.createSheet("Data");
            Row hdr = sheet.createRow(0);
            hdr.createCell(0).setCellValue("name");
            hdr.createCell(1).setCellValue("price");
            for (int i = 0; i < dataRows; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue("Item" + i);
                row.createCell(1).setCellValue(i * 1.5);
            }
            wb.write(os);
        }
        return file;
    }

    private Path createCsv(int dataRows) throws IOException {
        Path file = tempDir.resolve("stream.csv");
        StringBuilder sb = new StringBuilder("name,price\n");
        for (int i = 0; i < dataRows; i++) {
            sb.append("Item").append(i).append(",").append(i * 1.5).append("\n");
        }
        Files.writeString(file, sb.toString());
        return file;
    }

    // -------- XLSX streaming --------

    @Test
    void streamXlsx_iteratesAllRows() throws Exception {
        Path file = createXlsx(10);
        List<Item> items = new ArrayList<>();

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            for (Item item : reader) {
                items.add(item);
            }
        }

        assertThat(items).hasSize(10);
        assertThat(items.get(0).name).isEqualTo("Item0");
        assertThat(items.get(9).name).isEqualTo("Item9");
    }

    @Test
    void streamXlsx_forEach() throws Exception {
        Path file = createXlsx(5);
        List<String> names = new ArrayList<>();

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            reader.forEach(item -> names.add(item.name));
        }

        assertThat(names).hasSize(5);
    }

    @Test
    void streamXlsx_javaStream() throws Exception {
        Path file = createXlsx(10);

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            long count = reader.stream()
                    .filter(item -> item.price > 5.0)
                    .count();
            assertThat(count).isGreaterThan(0);
        }
    }

    @Test
    void streamXlsx_batch() throws Exception {
        Path file = createXlsx(25);
        List<Integer> batchSizes = new ArrayList<>();

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            reader.batch(10).forEach(batch -> batchSizes.add(batch.size()));
        }

        assertThat(batchSizes).containsExactly(10, 10, 5);
    }

    // -------- CSV streaming --------

    @Test
    void streamCsv_iteratesAllRows() throws Exception {
        Path file = createCsv(8);
        List<Item> items = new ArrayList<>();

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            for (Item item : reader) {
                items.add(item);
            }
        }

        assertThat(items).hasSize(8);
        assertThat(items.get(0).name).isEqualTo("Item0");
    }

    // -------- early close --------

    @Test
    void streamXlsx_earlyClose_doesNotThrow() throws Exception {
        Path file = createXlsx(100);

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            int count = 0;
            for (Item item : reader) {
                count++;
                if (count >= 5) break;
            }
            assertThat(count).isEqualTo(5);
        }
        // close() should clean up without error
    }

    // -------- closed reader --------

    @Test
    void iterateAfterClose_throws() throws Exception {
        Path file = createXlsx(5);

        StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config);
        reader.close();

        assertThatThrownBy(reader::iterator)
                .isInstanceOf(SheetzException.class)
                .hasMessageContaining("closed");
    }

    // -------- empty file --------

    @Test
    void streamXlsx_emptyFile_returnsNoRows() throws Exception {
        Path file = createXlsx(0);
        List<Item> items = new ArrayList<>();

        try (StreamingReader<Item> reader = new StreamingReader<>(file, Item.class, config)) {
            for (Item item : reader) {
                items.add(item);
            }
        }

        assertThat(items).isEmpty();
    }
}
