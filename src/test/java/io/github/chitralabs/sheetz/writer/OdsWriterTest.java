package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.Format;
import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.exception.SheetzException;
import io.github.chitralabs.sheetz.reader.OdsReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class OdsWriterTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Sheetz.reset();
    }

    // -------- models --------

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

    public static class Dated {
        public String name;
        public LocalDate created;

        public Dated() {}

        public Dated(String name, LocalDate created) {
            this.name = name;
            this.created = created;
        }
    }

    // -------- tests --------

    @Test
    void writeOds_createsValidFile() {
        Path file = tempDir.resolve("out.ods");
        List<Product> data = List.of(
            new Product("Widget", 9.99, true),
            new Product("Gadget", 24.50, false));

        Sheetz.write(data, file);

        assertThat(file).exists();
        List<Product> readBack = Sheetz.read(file, Product.class);
        assertThat(readBack).hasSize(2);
        assertThat(readBack.get(0).name).isEqualTo("Widget");
        assertThat(readBack.get(0).price).isEqualTo(9.99);
    }

    @Test
    void writeOds_viaOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Product> data = List.of(new Product("Test", 5.0, true));

        Sheetz.write(data, baos, Format.ODS);

        byte[] bytes = baos.toByteArray();
        assertThat(bytes.length).isGreaterThan(0);

        // Read back from byte array
        List<Product> readBack = Sheetz.read(new ByteArrayInputStream(bytes), Product.class, Format.ODS);
        assertThat(readBack).hasSize(1);
        assertThat(readBack.get(0).name).isEqualTo("Test");
    }

    @Test
    void writeOds_emptyData_throws() {
        Path file = tempDir.resolve("empty.ods");

        assertThatThrownBy(() -> Sheetz.write(List.of(), file))
            .isInstanceOf(SheetzException.class);
    }

    @Test
    void writeOds_nullData_throws() {
        Path file = tempDir.resolve("null.ods");

        assertThatThrownBy(() -> Sheetz.write(null, file))
            .isInstanceOf(SheetzException.class);
    }

    @Test
    void writeOds_customSheetName() {
        Path file = tempDir.resolve("named.ods");
        List<Product> data = List.of(new Product("A", 1.0, true));

        new OdsWriter<>(Product.class, SheetzConfig.defaults())
            .sheetName("Inventory")
            .write(data, file);

        assertThat(file).exists();
        List<Product> readBack = Sheetz.read(file, Product.class);
        assertThat(readBack).hasSize(1);
    }

    @Test
    void writeOds_viaWriterBuilder() {
        Path file = tempDir.resolve("builder.ods");

        Sheetz.writer(Product.class)
            .data(List.of(new Product("A", 1.0, true)))
            .file(file)
            .sheet("MySheet")
            .write();

        assertThat(file).exists();
        List<Product> readBack = Sheetz.read(file, Product.class);
        assertThat(readBack).hasSize(1);
    }

    @Test
    void writeOds_multiSheetWorkbook() {
        Path file = tempDir.resolve("multi.ods");

        Sheetz.workbook()
            .sheet("Products", List.of(new Product("Widget", 9.99, true)))
            .sheet("More", List.of(new Product("Gadget", 24.50, false)))
            .write(file.toString());

        assertThat(file).exists();
        // Read back the first sheet
        List<Product> readBack = Sheetz.read(file, Product.class);
        assertThat(readBack).hasSize(1);
        assertThat(readBack.get(0).name).isEqualTo("Widget");
    }

    @Test
    void writeOds_withNullFields_writesBlankCells() {
        Path file = tempDir.resolve("nulls.ods");
        List<Product> data = List.of(new Product(null, null, null));

        Sheetz.write(data, file);

        assertThat(file).exists();
    }
}
