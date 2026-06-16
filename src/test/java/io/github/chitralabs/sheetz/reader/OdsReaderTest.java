package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.Sheetz;
import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.writer.OdsWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class OdsReaderTest {

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

    public static class AnnotatedProduct {
        @Column("Product Name")
        public String name;

        @Column(index = 1)
        public Double price;

        public AnnotatedProduct() {}
    }

    // -------- helpers --------

    private Path writeOds(List<Product> data) {
        Path file = tempDir.resolve("test.ods");
        new OdsWriter<>(Product.class, SheetzConfig.defaults()).write(data, file);
        return file;
    }

    // -------- tests --------

    @Test
    void readOds_roundtrip() {
        List<Product> data = List.of(
            new Product("Widget", 9.99, true),
            new Product("Gadget", 24.50, false));
        Path file = writeOds(data);

        List<Product> result = Sheetz.read(file, Product.class);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name).isEqualTo("Widget");
        assertThat(result.get(0).price).isEqualTo(9.99);
        assertThat(result.get(0).inStock).isTrue();
        assertThat(result.get(1).name).isEqualTo("Gadget");
        assertThat(result.get(1).price).isEqualTo(24.50);
        assertThat(result.get(1).inStock).isFalse();
    }

    @Test
    void readOds_viaStringPath() {
        Path file = writeOds(List.of(new Product("A", 1.0, true)));

        List<Product> result = Sheetz.read(file.toString(), Product.class);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name).isEqualTo("A");
    }

    @Test
    void readOds_readMaps() {
        Path file = writeOds(List.of(new Product("Widget", 9.99, true)));

        List<Map<String, Object>> result = Sheetz.readMaps(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("Widget");
        assertThat(result.get(0).get("price")).isEqualTo(9.99);
    }

    @Test
    void readOds_readRaw() {
        Path file = writeOds(List.of(new Product("Widget", 9.99, true)));

        List<String[]> result = Sheetz.readRaw(file);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        // Header row
        assertThat(result.get(0)).contains("name");
    }

    @Test
    void readOds_viaReaderBuilder() {
        Path file = writeOds(List.of(new Product("Widget", 9.99, true)));

        List<Product> result = Sheetz.reader(Product.class)
            .file(file)
            .read();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name).isEqualTo("Widget");
    }

    @Test
    void readOds_validate() {
        Path file = writeOds(List.of(new Product("Widget", 9.99, true)));

        var result = Sheetz.validate(file, Product.class);

        assertThat(result.isValid()).isTrue();
        assertThat(result.validRows()).hasSize(1);
    }

    @Test
    void streamOds_fallsBackToInMemory() {
        Path file = writeOds(List.of(
            new Product("A", 1.0, true),
            new Product("B", 2.0, false)));

        List<Product> result = new java.util.ArrayList<>();
        try (var reader = Sheetz.stream(file, Product.class)) {
            reader.forEach(result::add);
        }

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name).isEqualTo("A");
        assertThat(result.get(1).name).isEqualTo("B");
    }
}
