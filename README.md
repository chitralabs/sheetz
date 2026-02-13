# Sheetz — Excel & CSV Processing for Java, Simplified

[![Build](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml/badge.svg)](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.chitralabs.sheetz/sheetz-core)](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/chitralabs/sheetz?style=social)](https://github.com/chitralabs/sheetz/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/chitralabs/sheetz)](https://github.com/chitralabs/sheetz/issues)

**Read and write Excel (.xlsx, .xls) and CSV files in Java with one line of code.**

Sheetz replaces hundreds of lines of Apache POI boilerplate with a clean, annotation-based API. Stream million-row files with constant memory. Validate data with row-level error reports. Ship your spreadsheet feature in minutes, not days.

```java
// Read an Excel file into Java objects — that's it
List<Product> products = Sheetz.read("products.xlsx", Product.class);

// Write Java objects to Excel — that's it
Sheetz.write(products, "products.xlsx");
```

> **New to Sheetz?** Jump to [Quick Start](#quick-start) or browse [8 runnable examples](https://github.com/chitralabs/sheetz-examples).

---

## The Problem

If you've built a Java backend that imports or exports spreadsheets, you know the pain:

**Apache POI** makes you manually create workbooks, sheets, rows, and cells. A simple "export these objects to Excel" operation takes 25+ lines of error-prone code. Reading is worse — you iterate rows, check cell types, cast values, and handle nulls yourself.

```java
// Apache POI: 25+ lines just to write a list of products
Workbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("Products");
Row header = sheet.createRow(0);
header.createCell(0).setCellValue("Name");
header.createCell(1).setCellValue("Price");
header.createCell(2).setCellValue("In Stock");
// ... 20 more lines of cell-by-cell manual work
```

**Sheetz replaces all of that with one line:**

```java
Sheetz.write(products, "products.xlsx");
```

No workbook management. No cell iteration. No type casting. Just data in, spreadsheet out.

---

## Why Sheetz

| | Apache POI | EasyExcel | Sheetz |
|---|---|---|---|
| Write 10 products to Excel | ~25 lines | ~3 lines | **1 line** |
| Read Excel into typed objects | ~20 lines | ~12 lines (listener) | **1 line** |
| Total code for read + write | ~60 lines | ~30 lines | **~15 lines** |
| Streaming large files | Manual SXSSF setup | Built-in | **Built-in** |
| Data validation with errors | Not included | Not included | **Built-in** |
| Multi-sheet workbook | Manual | Multiple calls | **Fluent builder** |
| Learning curve | Steep | Moderate | **Minimal** |

**100K-row write benchmark:** Sheetz completes in **424ms** vs Apache POI's **2,453ms** — 5.8x faster — while using 1 line of code instead of 25. ([Full benchmarks](https://github.com/chitralabs/sheetz-benchmarks))

---

## Features

- **One-Line API** — `Sheetz.read()`, `Sheetz.write()`, `Sheetz.stream()`, `Sheetz.validate()`. No boilerplate.
- **Annotation Mapping** — `@Column` with custom headers, required fields, defaults, custom formats, and converters.
- **True Streaming** — SAX-based XLSX parsing with constant ~10MB memory. Process million-row files without OutOfMemoryError.
- **Batch Processing** — `stream().batch(1000).forEach(batch -> db.bulkInsert(batch))` for ETL pipelines.
- **Built-in Validation** — Row-level error reporting with column name, value, and cause. Get success rates and valid rows.
- **19 Type Converters** — Primitives, BigDecimal, LocalDate, LocalDateTime, Instant, ZonedDateTime, UUID, Enum, and more.
- **Multi-Sheet Workbooks** — `Sheetz.workbook().sheet("A", listA).sheet("B", listB).write("report.xlsx")`
- **Three Formats** — XLSX (Excel 2007+), XLS (Excel 97-2003), and CSV. Auto-detected from file extension.
- **Fluent Builders** — `ReaderBuilder` and `WriterBuilder` for sheet selection, header row, auto-sizing, freeze panes, delimiters.
- **Thread-Safe** — Safe for concurrent use in multi-threaded and Spring Boot applications.
- **Zero Config** — Works out of the box. Add the dependency, call the method, done.

---

## Quick Start

### 1. Add the dependency

**Maven:**
```xml
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.chitralabs.sheetz:sheetz-core:1.0.1'
```

### 2. Define your model

```java
public class Product {
    public String name;
    public Double price;
    public Boolean inStock;
    public LocalDate releaseDate;

    public Product() {}
}
```

### 3. Read and write

```java
// Write to Excel
Sheetz.write(products, "products.xlsx");

// Read from Excel
List<Product> products = Sheetz.read("products.xlsx", Product.class);

// Read from CSV (auto-detected)
List<Product> products = Sheetz.read("products.csv", Product.class);
```

That's it. No configuration required.

---

## Usage Guide

### Reading Spreadsheets

```java
// Read Excel/CSV into typed objects
List<Product> products = Sheetz.read("products.xlsx", Product.class);

// Read as Maps (no model class needed)
List<Map<String, Object>> data = Sheetz.readMaps("products.xlsx");

// Read raw string arrays
List<String[]> rows = Sheetz.readRaw("products.xlsx");

// Read only the first N rows
List<Product> sample = Sheetz.readFirst("products.xlsx", Product.class, 100);
```

### Writing Spreadsheets

```java
// Write to Excel
Sheetz.write(products, "products.xlsx");

// Write to CSV
Sheetz.write(products, "products.csv");

// Write to legacy Excel format
Sheetz.write(products, "products.xls");
```

### Streaming Large Files

For files over 100K rows, use `stream()` to maintain constant memory:

```java
// Process row by row — constant ~10MB memory
Sheetz.stream("huge-file.xlsx", Product.class)
    .forEach(product -> process(product));

// Batch insert into a database
Sheetz.stream("huge-file.xlsx", Product.class)
    .batch(1000)
    .forEach(batch -> database.bulkInsert(batch));

// Use Java Streams API
long expensiveCount = Sheetz.stream("huge-file.xlsx", Product.class)
    .stream()
    .filter(p -> p.price > 100)
    .count();
```

### Validating Data

```java
ValidationResult<Product> result = Sheetz.validate("import.csv", Product.class);

System.out.println("Valid: " + result.validCount());
System.out.println("Errors: " + result.errorCount());
System.out.println("Success rate: " + result.successRate() + "%");

for (ValidationResult.RowError error : result.errors()) {
    System.out.println("Row " + error.row() + ", Column '" + error.column()
        + "': " + error.message());
}

// Get only the valid rows
List<Product> validProducts = result.validRows();
```

### Annotation Mapping

```java
public class Employee {
    @Column("Full Name")                    // Map to a different header
    public String name;

    @Column(index = 1)                      // Map by column position
    public Double salary;

    @Column(required = true)                // Fail validation if empty
    public String employeeId;

    @Column(defaultValue = "active")        // Default for empty cells
    public String status;

    @Column(format = "dd/MM/yyyy")          // Custom date format
    public LocalDate startDate;

    @Column(converter = MoneyConverter.class) // Custom converter
    public BigDecimal bonus;

    @Column(ignore = true)                  // Skip during read/write
    public String internalNotes;

    @Column(width = 30)                     // Column width (write only)
    public String address;
}
```

### Builder API

For fine-grained control over reading and writing:

```java
// Reader with options
List<Product> products = Sheetz.reader(Product.class)
    .file("products.xlsx")
    .sheet("Inventory")       // Select sheet by name
    .headerRow(1)             // Header on row 2 (0-based)
    .delimiter(';')           // For CSV files
    .read();

// Writer with options
Sheetz.writer(Product.class)
    .data(products)
    .file("output.xlsx")
    .sheet("Products")        // Custom sheet name
    .autoSize(true)           // Auto-fit column widths
    .freezeHeader(true)       // Freeze the header row
    .streaming(true)          // Force SXSSF streaming mode
    .write();

// Multi-sheet workbook
Sheetz.workbook()
    .sheet("Products", products)
    .sheet("Employees", employees)
    .sheet("Orders", orders)
    .write("report.xlsx");
```

### Configuration

```java
SheetzConfig config = SheetzConfig.builder()
    .dateFormat("dd/MM/yyyy")
    .dateTimeFormat("dd/MM/yyyy HH:mm")
    .trimValues(true)
    .skipEmptyRows(true)
    .defaultSheetName("Data")
    .streamingThreshold(10000)
    .evaluateFormulas(true)
    .charset(StandardCharsets.ISO_8859_1)
    .build();

Sheetz.configure(config);
```

### Custom Converters

```java
public class MoneyConverter implements Converter<BigDecimal> {
    @Override
    public BigDecimal fromCell(Object value, ConvertContext ctx) {
        if (value == null) return null;
        String str = value.toString().replace("$", "").replace(",", "").trim();
        return new BigDecimal(str);
    }

    @Override
    public Object toCell(BigDecimal value) {
        return value != null ? "$" + value.setScale(2, RoundingMode.HALF_UP) : null;
    }
}

// Use per-field
@Column(converter = MoneyConverter.class)
public BigDecimal price;

// Or register globally for a type
Sheetz.register(Money.class, new MoneyConverter());
```

---

## Supported Types

Sheetz automatically converts between Excel cells and these Java types:

| Type | Example Values | Notes |
|------|---------------|-------|
| `String` | Any text | Trimmed by default |
| `int` / `Integer` | `42`, `42.0` | Decimals truncated |
| `long` / `Long` | `123456789012` | Large numbers |
| `double` / `Double` | `3.14159` | Standard decimals |
| `float` / `Float` | `2.5` | Single precision |
| `short` / `Short` | `32767` | |
| `byte` / `Byte` | `127` | |
| `char` / `Character` | `A` | First character |
| `boolean` / `Boolean` | `true`, `yes`, `y`, `1`, `on` | Case-insensitive |
| `BigDecimal` | `99.99` | Precise decimals |
| `BigInteger` | `999999999999999` | Arbitrary precision |
| `LocalDate` | `2024-01-15` | Configurable format |
| `LocalDateTime` | `2024-01-15 10:30:00` | Configurable format |
| `LocalTime` | `10:30:00` | ISO format |
| `Instant` | `2024-01-15T10:30:00Z` | UTC timestamp |
| `ZonedDateTime` | `2024-01-15T10:30:00+01:00` | With timezone |
| `Date` (legacy) | `2024-01-15` | java.util.Date |
| `UUID` | `550e8400-e29b-...` | Standard format |
| `Enum` | `ACTIVE`, `active` | Case-insensitive |

---

## Supported Formats

| Format | Extension | Read | Write | Streaming Read | Streaming Write |
|--------|-----------|:----:|:-----:|:--------------:|:---------------:|
| Excel 2007+ | `.xlsx` | Yes | Yes | Yes (SAX) | Yes (SXSSF) |
| Excel 97-2003 | `.xls` | Yes | Yes | — | — |
| CSV | `.csv` | Yes | Yes | Yes (buffered) | Yes |

Format is auto-detected from the file extension. When using streams, specify the format explicitly:

```java
Sheetz.read(inputStream, Product.class, Format.XLSX);
Sheetz.write(products, outputStream, Format.CSV);
```

---

## Memory Efficiency

| Method | Small files (<10K rows) | Large files (>10K rows) |
|--------|:-----------------------:|:-----------------------:|
| `read()` | Full load | Full load |
| `write()` | Standard | Auto-SXSSF streaming |
| `stream()` | SAX streaming (~10MB) | SAX streaming (~10MB) |

For files over 100K rows, always prefer `stream()`:

```java
// Constant memory, any file size
Sheetz.stream("million-rows.xlsx", Product.class)
    .forEach(this::process);
```

---

## Performance

Benchmarked with JMH against the most popular Java Excel libraries. ([Full results and methodology](https://github.com/chitralabs/sheetz-benchmarks))

### Write Performance (ms/op, lower is better)

| Rows | Sheetz | Apache POI | EasyExcel | FastExcel |
|-----:|-------:|-----------:|----------:|----------:|
| 1K | 23 | 22 | 11 | 6 |
| 10K | 233 | 217 | 59 | 32 |
| **100K** | **424** | **2,453** | **543** | **310** |

At 100K rows, Sheetz is **5.8x faster than Apache POI** while requiring **1 line of code** instead of 25.

### Read Performance (ms/op, lower is better)

| Rows | Sheetz | Apache POI | EasyExcel | FastExcel | Poiji |
|-----:|-------:|-----------:|----------:|----------:|------:|
| 1K | 13 | 11 | 5 | 2 | 12 |
| 10K | 128 | 106 | 43 | 25 | 115 |
| 100K | 1,286 | 1,097 | 334 | 210 | 1,042 |

**The tradeoff:** Sheetz prioritizes developer experience and code simplicity. Libraries like FastExcel and EasyExcel are faster at raw throughput, but require significantly more code and offer fewer features (no validation, no multi-format support, limited type conversion). Choose the tool that fits your priorities.

---

## Use Cases

**Data Import / Upload Processing**
Handle user-uploaded Excel or CSV files in web applications. Validate, report errors, and import clean data.

**Report Generation / Data Export**
Generate Excel reports from database queries. Multi-sheet workbooks, auto-sized columns, frozen headers — all in a few lines.

**ETL Pipelines**
Stream millions of rows from spreadsheets into databases with constant memory using `stream().batch()`.

**Spreadsheet Format Conversion**
Convert between XLSX, XLS, and CSV formats: `Sheetz.write(Sheetz.read("data.xlsx", Row.class), "data.csv")`

**Data Validation**
Validate spreadsheet data before import. Get per-row error details with column names, invalid values, and root causes.

**Spring Boot APIs**
Build upload/download endpoints for Excel files. Sheetz is thread-safe and works directly with `InputStream`/`OutputStream`.

---

## Examples

The [sheetz-examples](https://github.com/chitralabs/sheetz-examples) repository contains 8 runnable demos:

| # | Example | What it covers |
|---|---------|---------------|
| 01 | Basic Read & Write | `read()`, `write()`, `readMaps()`, `readRaw()` |
| 02 | Annotation Mapping | `@Column` with headers, required, defaults, format, ignore, width |
| 03 | Streaming Large Files | `stream()`, `batch()`, constant-memory processing |
| 04 | Data Validation | `validate()`, error reporting, success rates |
| 05 | Builder API | `ReaderBuilder`, `WriterBuilder`, sheet selection, auto-size |
| 06 | Multi-Sheet Workbook | `WorkbookBuilder` with multiple model types |
| 07 | Custom Converter | `MoneyConverter`, global converter registration |
| 08 | Format Conversion | XLSX to CSV, CSV to XLS, batch conversion |

```bash
git clone https://github.com/chitralabs/sheetz-examples.git
cd sheetz-examples
mvn compile exec:java -Dexec.mainClass="io.github.chitralabs.sheetz.examples.E01_BasicReadWrite"
```

---

## Roadmap

- [ ] `sheetz-spring-boot-starter` — Auto-configuration for Spring Boot with `@EnableSheetz`
- [ ] Google Sheets API integration — Read/write directly from Google Sheets
- [ ] Excel formula support in write operations
- [ ] Template-based writing — Fill data into existing Excel templates
- [ ] Async streaming with CompletableFuture
- [ ] Column-level style annotations (bold, color, number format)

Have an idea? [Open an issue](https://github.com/chitralabs/sheetz/issues) — contributions and suggestions are welcome.

---

## Requirements

- Java 11 or higher
- No other setup needed — Sheetz bundles Apache POI and OpenCSV internally

---

## Building from Source

```bash
git clone https://github.com/chitralabs/sheetz.git
cd sheetz
mvn clean install
```

## Contributing

Contributions are welcome! Whether it's a bug report, feature request, documentation improvement, or code contribution — all help is appreciated.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to get started.

## Security

To report a security vulnerability, please see [SECURITY.md](SECURITY.md). Do not open a public issue for security reports.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for release history.

## License

[Apache License 2.0](LICENSE) — free for commercial and personal use.

---

If Sheetz saved you time or simplified your codebase, consider giving it a star. It helps other developers find this project and motivates continued development.

[![Star this repo](https://img.shields.io/github/stars/chitralabs/sheetz?style=social)](https://github.com/chitralabs/sheetz)
