package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CsvWriterTest {

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
        public Integer qty;

        public Item() {}

        public Item(String name, Double price, Integer qty) {
            this.name = name;
            this.price = price;
            this.qty = qty;
        }
    }

    // -------- write to Path --------

    @Test
    void writeToPath_createsValidCsv() throws IOException {
        Path csv = tempDir.resolve("out.csv");
        List<Item> data = List.of(new Item("Widget", 9.99, 5), new Item("Gadget", 24.50, 12));

        new CsvWriter<>(Item.class, config).write(data, csv);

        String content = Files.readString(csv);
        assertThat(content).contains("name");
        assertThat(content).contains("Widget");
        assertThat(content).contains("Gadget");
        assertThat(content.lines().count()).isEqualTo(3); // header + 2 data rows
    }

    // -------- write to OutputStream --------

    @Test
    void writeToOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Item> data = List.of(new Item("Bolt", 0.10, 100));

        new CsvWriter<>(Item.class, config).write(data, baos);

        String output = baos.toString();
        assertThat(output).contains("Bolt");
        assertThat(output).contains("0.1");
    }

    // -------- custom delimiter --------

    @Test
    void writeWithSemicolonDelimiter() throws IOException {
        Path csv = tempDir.resolve("semi.csv");
        List<Item> data = List.of(new Item("A", 1.0, 1));

        new CsvWriter<>(Item.class, config).delimiter(';').write(data, csv);

        String content = Files.readString(csv);
        assertThat(content).contains(";");
    }

    // -------- null handling --------

    @Test
    void writeWithNullFields_writesEmptyStrings() throws IOException {
        Path csv = tempDir.resolve("nulls.csv");
        List<Item> data = List.of(new Item(null, null, null));

        new CsvWriter<>(Item.class, config).write(data, csv);

        String content = Files.readString(csv);
        assertThat(content.lines().count()).isEqualTo(2);
    }

    // -------- validation --------

    @Test
    void writeEmptyList_throwsSheetzException() {
        Path csv = tempDir.resolve("fail.csv");

        assertThatThrownBy(() -> new CsvWriter<>(Item.class, config).write(List.of(), csv))
                .isInstanceOf(SheetzException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void writeNull_throwsSheetzException() {
        Path csv = tempDir.resolve("fail.csv");

        assertThatThrownBy(() -> new CsvWriter<>(Item.class, config).write(null, csv))
                .isInstanceOf(SheetzException.class);
    }

    // -------- annotation mapping --------

    public static class Annotated {
        @Column("Product Name")
        public String name;

        @Column(ignore = true)
        public String internal;

        public Double price;

        public Annotated() {}

        public Annotated(String name, String internal, Double price) {
            this.name = name;
            this.internal = internal;
            this.price = price;
        }
    }

    @Test
    void writeWithAnnotations_usesHeaderNamesAndSkipsIgnored() throws IOException {
        Path csv = tempDir.resolve("ann.csv");
        List<Annotated> data = List.of(new Annotated("Widget", "secret", 5.0));

        new CsvWriter<>(Annotated.class, config).write(data, csv);

        String content = Files.readString(csv);
        assertThat(content).contains("Product Name");
        assertThat(content).doesNotContain("internal");
        assertThat(content).doesNotContain("secret");
    }
}
