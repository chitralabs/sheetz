package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.exception.MappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CsvReaderTest {

    private SheetzConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = SheetzConfig.defaults();
    }

    // -------- simple model --------

    public static class Item {
        public String name;
        public Double price;
        public Integer quantity;

        public Item() {}
    }

    // -------- read(Path) --------

    @Test
    void readFromPath_mapsColumnsToFields() throws IOException {
        Path csv = tempDir.resolve("items.csv");
        Files.writeString(csv, "name,price,quantity\nWidget,9.99,5\nGadget,24.50,12\n");

        List<Item> items = new CsvReader<>(Item.class, config).read(csv);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).name).isEqualTo("Widget");
        assertThat(items.get(0).price).isEqualTo(9.99);
        assertThat(items.get(0).quantity).isEqualTo(5);
        assertThat(items.get(1).name).isEqualTo("Gadget");
    }

    @Test
    void readFromPath_emptyFile_returnsEmptyList() throws IOException {
        Path csv = tempDir.resolve("empty.csv");
        Files.writeString(csv, "");

        List<Item> items = new CsvReader<>(Item.class, config).read(csv);

        assertThat(items).isEmpty();
    }

    @Test
    void readFromPath_headerOnly_returnsEmptyList() throws IOException {
        Path csv = tempDir.resolve("header.csv");
        Files.writeString(csv, "name,price,quantity\n");

        List<Item> items = new CsvReader<>(Item.class, config).read(csv);

        assertThat(items).isEmpty();
    }

    @Test
    void readFromPath_skipsEmptyRows() throws IOException {
        Path csv = tempDir.resolve("blanks.csv");
        Files.writeString(csv, "name,price,quantity\n,,\nWidget,1.0,1\n");

        List<Item> items = new CsvReader<>(Item.class, config).read(csv);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).name).isEqualTo("Widget");
    }

    // -------- custom delimiter --------

    @Test
    void readWithSemicolonDelimiter() throws IOException {
        Path csv = tempDir.resolve("semi.csv");
        Files.writeString(csv, "name;price;quantity\nBolt;0.10;1000\n");

        List<Item> items = new CsvReader<>(Item.class, config).delimiter(';').read(csv);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).name).isEqualTo("Bolt");
        assertThat(items.get(0).price).isEqualTo(0.10);
    }

    // -------- read(InputStream) --------

    @Test
    void readFromInputStream() {
        String data = "name,price,quantity\nFoo,5.0,3\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        List<Item> items = new CsvReader<>(Item.class, config).read(bais);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).name).isEqualTo("Foo");
    }

    // -------- readMaps --------

    @Test
    void readMaps_returnsHeaderKeyedMaps() throws IOException {
        Path csv = tempDir.resolve("maps.csv");
        Files.writeString(csv, "col1,col2\nA,B\nC,D\n");

        List<Map<String, Object>> maps = CsvReader.readMaps(csv, config);

        assertThat(maps).hasSize(2);
        assertThat(maps.get(0)).containsEntry("col1", "A").containsEntry("col2", "B");
    }

    // -------- readRaw --------

    @Test
    void readRaw_returnsStringArraysIncludingHeader() throws IOException {
        Path csv = tempDir.resolve("raw.csv");
        Files.writeString(csv, "a,b\n1,2\n");

        List<String[]> rows = CsvReader.readRaw(csv, config);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("a", "b");
        assertThat(rows.get(1)).containsExactly("1", "2");
    }

    // -------- annotation mapping --------

    public static class Annotated {
        @Column("Product Name")
        public String name;

        @Column(required = true)
        public String sku;

        @Column(defaultValue = "active")
        public String status;

        public Annotated() {}
    }

    @Test
    void readWithAnnotations_mapsHeaderAndAppliesDefaults() throws IOException {
        Path csv = tempDir.resolve("ann.csv");
        Files.writeString(csv, "Product Name,sku,status\nWidget,W001,\n");

        List<Annotated> list = new CsvReader<>(Annotated.class, config).read(csv);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).name).isEqualTo("Widget");
        assertThat(list.get(0).sku).isEqualTo("W001");
        assertThat(list.get(0).status).isEqualTo("active");
    }

    @Test
    void readWithRequired_throwsWhenEmpty() throws IOException {
        Path csv = tempDir.resolve("req.csv");
        Files.writeString(csv, "Product Name,sku,status\nWidget,,sold\n");

        assertThatThrownBy(() -> new CsvReader<>(Annotated.class, config).read(csv))
                .isInstanceOf(MappingException.class)
                .hasMessageContaining("Required field is empty");
    }

    // -------- unicode --------

    @Test
    void readWithUnicodeContent() throws IOException {
        Path csv = tempDir.resolve("unicode.csv");
        Files.writeString(csv, "name,price,quantity\n日本語,100.0,1\n");

        List<Item> items = new CsvReader<>(Item.class, config).read(csv);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).name).isEqualTo("日本語");
    }
}
