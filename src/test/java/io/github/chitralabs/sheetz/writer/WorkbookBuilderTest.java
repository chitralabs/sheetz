package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class WorkbookBuilderTest {

    private SheetzConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = SheetzConfig.defaults();
    }

    // -------- models --------

    public static class Product {
        public String name;
        public Double price;

        public Product() {}

        public Product(String name, Double price) {
            this.name = name;
            this.price = price;
        }
    }

    public static class Employee {
        public String firstName;
        public String role;

        public Employee() {}

        public Employee(String firstName, String role) {
            this.firstName = firstName;
            this.role = role;
        }
    }

    // -------- multi-sheet --------

    @Test
    void multiSheet_createsAllSheets() throws IOException {
        Path file = tempDir.resolve("multi.xlsx");

        new WorkbookBuilder(config)
                .sheet("Products", List.of(new Product("Widget", 9.99)))
                .sheet("Staff", List.of(new Employee("Alice", "Engineer")))
                .write(file);

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file.toFile()))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(2);
            assertThat(wb.getSheetName(0)).isEqualTo("Products");
            assertThat(wb.getSheetName(1)).isEqualTo("Staff");

            Sheet products = wb.getSheetAt(0);
            assertThat(products.getRow(0).getCell(0).getStringCellValue()).isEqualTo("name");
            assertThat(products.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Widget");

            Sheet staff = wb.getSheetAt(1);
            assertThat(staff.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Alice");
        }
    }

    // -------- validation --------

    @Test
    void noSheets_throws() {
        Path file = tempDir.resolve("empty.xlsx");

        assertThatThrownBy(() -> new WorkbookBuilder(config).write(file))
                .isInstanceOf(SheetzException.class)
                .hasMessageContaining("No sheets");
    }

    @Test
    void csvFormat_throws() {
        Path file = tempDir.resolve("bad.csv");

        assertThatThrownBy(() -> new WorkbookBuilder(config)
                .sheet("Data", List.of(new Product("A", 1.0)))
                .write(file))
                .isInstanceOf(SheetzException.class)
                .hasMessageContaining("CSV");
    }

    @Test
    void emptySheetData_throws() {
        assertThatThrownBy(() -> new WorkbookBuilder(config)
                .sheet("Empty", List.of()))
                .isInstanceOf(SheetzException.class);
    }

    // -------- string path overload --------

    @Test
    void writeWithStringPath() throws IOException {
        Path file = tempDir.resolve("str.xlsx");

        new WorkbookBuilder(config)
                .sheet("Data", List.of(new Product("Test", 1.0)))
                .write(file.toString());

        assertThat(file).exists();
    }
}
