# Sheetz

[![Build](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml/badge.svg)](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.chitralabs.sheetz/sheetz-core)](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

**High-performance Excel and CSV processing for Java** — Read and write spreadsheets with one line of code.

**See it in action:** Check out the [sheetz-examples](https://github.com/chitralabs/sheetz-examples) repo for 8 runnable demos covering every feature.

**How does it compare?** See [sheetz-benchmarks](https://github.com/chitralabs/sheetz-benchmarks) for side-by-side code and JMH performance comparisons against Apache POI, EasyExcel, FastExcel, and Poiji.

## Features

- 🚀 **One-liner API** — No boilerplate, just `Sheetz.read()` and `Sheetz.write()`
- 📊 **True Streaming** — Process million-row files with constant ~10MB memory
- 🔄 **Auto Type Conversion** — 19 built-in converters including dates, enums, and BigDecimal
- 🧵 **Thread-Safe** — Safe for concurrent use in multi-threaded applications
- ✅ **Validation** — Detailed error reporting with row/column context
- 📝 **Annotations** — `@Column` for custom mapping, required fields, defaults

## Quick Start

### Maven

```xml
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.chitralabs.sheetz:sheetz-core:1.0.1'
```

## Usage

### Define Your Model

```java
public class Product {
    public String name;
    public Double price;
    public Boolean inStock;
    public LocalDate releaseDate;
    
    public Product() {} // Required no-arg constructor
}
```

### Read Excel/CSV

```java
// Read from file
List<Product> products = Sheetz.read("products.xlsx", Product.class);

// Read from CSV
List<Product> products = Sheetz.read("products.csv", Product.class);

// Read as Maps (no model needed)
List<Map<String, Object>> data = Sheetz.readMaps("products.xlsx");

// Read raw strings
List<String[]> rows = Sheetz.readRaw("products.xlsx");
```

### Write Excel/CSV

```java
List<Product> products = List.of(
    new Product("Widget", 19.99, true, LocalDate.of(2024, 1, 15)),
    new Product("Gadget", 49.99, false, LocalDate.of(2024, 2, 20))
);

// Write to Excel
Sheetz.write(products, "products.xlsx");

// Write to CSV
Sheetz.write(products, "products.csv");

// Write to legacy Excel format
Sheetz.write(products, "products.xls");
```

### Stream Large Files (Memory Efficient)

```java
// Process row by row — constant memory usage
Sheetz.stream("huge-file.xlsx", Product.class)
    .forEach(product -> process(product));

// Process in batches for bulk operations
Sheetz.stream("huge-file.xlsx", Product.class)
    .batch(1000)
    .forEach(batch -> database.bulkInsert(batch));

// Use Java Streams
long count = Sheetz.stream("huge-file.xlsx", Product.class)
    .stream()
    .filter(p -> p.price > 100)
    .count();
```

### Validate Data

```java
ValidationResult<Product> result = Sheetz.validate("products.csv", Product.class);

if (result.hasErrors()) {
    System.out.println("Valid rows: " + result.validCount());
    System.out.println("Errors: " + result.errorCount());
    System.out.println("Success rate: " + result.successRate() + "%");
    
    for (ValidationResult.RowError error : result.errors()) {
        System.out.println("Row " + error.row() + ", Column '" + error.column() + 
                           "': " + error.message());
    }
}

// Get valid rows only
List<Product> validProducts = result.validRows();
```

## Annotations

Use `@Column` to customize field mapping:

```java
public class Product {
    @Column("Product Name")  // Map to different header name
    public String name;
    
    @Column(index = 1)  // Map by column index (0-based)
    public Double price;
    
    @Column(required = true)  // Fail validation if empty
    public String sku;
    
    @Column(defaultValue = "pending")  // Default for empty cells
    public String status;
    
    @Column(format = "dd/MM/yyyy")  // Custom date format
    public LocalDate orderDate;
    
    @Column(converter = MoneyConverter.class)  // Custom converter
    public BigDecimal amount;
    
    @Column(ignore = true)  // Skip this field
    public String internalId;
    
    @Column(width = 20)  // Column width in characters (write only)
    public String description;
}
```

## Builder API

For more control, use the builder pattern:

### Reader Builder

```java
List<Product> products = Sheetz.reader(Product.class)
    .file("products.xlsx")
    .sheet("Inventory")       // Select sheet by name
    .sheet(0)                 // Or by index (0-based)
    .headerRow(1)             // Header on row 2 (0-based)
    .delimiter(';')           // For CSV files
    .read();
```

### Writer Builder

```java
Sheetz.writer(Product.class)
    .data(products)
    .file("output.xlsx")
    .sheet("Products")        // Custom sheet name
    .autoSize(true)           // Auto-fit column widths
    .freezeHeader(true)       // Freeze the header row
    .streaming(true)          // Force streaming mode (SXSSF)
    .write();
```

### Multi-Sheet Workbook

```java
Sheetz.workbook()
    .sheet("Products", products)
    .sheet("Employees", employees)
    .sheet("Orders", orders)
    .write("report.xlsx");
```

## Configuration

Customize global settings:

```java
SheetzConfig config = SheetzConfig.builder()
    .dateFormat("dd/MM/yyyy")           // Default: yyyy-MM-dd
    .dateTimeFormat("dd/MM/yyyy HH:mm") // Default: yyyy-MM-dd HH:mm:ss
    .trimValues(true)                   // Trim whitespace (default: true)
    .skipEmptyRows(true)                // Skip blank rows (default: true)
    .headerRow(0)                       // Header row index (default: 0)
    .defaultSheetName("Data")           // Default: Sheet1
    .streamingThreshold(10000)          // Auto-stream above this (default: 10000)
    .evaluateFormulas(true)             // Evaluate formulas (default: true)
    .charset(StandardCharsets.ISO_8859_1) // CSV encoding (default: UTF-8)
    .build();

Sheetz.configure(config);
```

## Custom Converters

Create custom converters for special types:

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

// Use with annotation
@Column(converter = MoneyConverter.class)
public BigDecimal price;

// Or register globally
Sheetz.register(Money.class, new MoneyConverter());
```

## Supported Types

Sheetz automatically converts these types:

| Type | Read Examples | Notes |
|------|---------------|-------|
| `String` | Any text | Trimmed if configured |
| `Integer` / `int` | `42`, `42.0` | Decimals truncated |
| `Long` / `long` | `123456789012` | Large numbers |
| `Double` / `double` | `3.14159` | Standard decimals |
| `Float` / `float` | `2.5` | Single precision |
| `Short` / `short` | `32767` | Small integers |
| `Byte` / `byte` | `127` | Tiny integers |
| `Character` / `char` | `A` | First character |
| `BigDecimal` | `99.99` | Precise decimals |
| `BigInteger` | `999999999999999` | Arbitrary precision |
| `Boolean` / `boolean` | `true`, `yes`, `y`, `1`, `on` | Case-insensitive |
| `LocalDate` | `2024-01-15` | Configurable format |
| `LocalDateTime` | `2024-01-15 10:30:00` | Configurable format |
| `LocalTime` | `10:30:00` | ISO format |
| `Instant` | `2024-01-15T10:30:00Z` | UTC timestamp |
| `ZonedDateTime` | `2024-01-15T10:30:00+01:00` | With timezone |
| `Date` (legacy) | `2024-01-15` | Uses LocalDateTime |
| `UUID` | `550e8400-e29b-41d4-a716-446655440000` | Standard format |
| `Enum` | `ACTIVE`, `active` | Case-insensitive |

## Thread Safety

Sheetz is designed for concurrent use:

```java
// Safe to use from multiple threads
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        List<Product> products = Sheetz.read("products.xlsx", Product.class);
        // Process products...
    });
}
```

## Memory Efficiency

| Operation | Small Files (<10K rows) | Large Files (>10K rows) |
|-----------|------------------------|-------------------------|
| `read()` | Loads all into memory | Loads all into memory |
| `write()` | Standard POI | Auto-uses SXSSF streaming |
| `stream()` | SAX streaming | SAX streaming (~10MB) |

For files over 100K rows, always use `stream()`:

```java
// ✅ Good - constant memory
Sheetz.stream("million-rows.xlsx", Product.class)
    .forEach(this::process);

// ❌ Bad - may cause OutOfMemoryError
List<Product> all = Sheetz.read("million-rows.xlsx", Product.class);
```

## Error Handling

```java
try {
    List<Product> products = Sheetz.read("products.xlsx", Product.class);
} catch (SheetzException e) {
    // File not found, unsupported format, etc.
    System.err.println("Error: " + e.getMessage());
}

// For detailed row-level errors, use validate()
ValidationResult<Product> result = Sheetz.validate("products.xlsx", Product.class);
for (ValidationResult.RowError error : result.errors()) {
    System.err.println("Row " + error.row() + ": " + error.message());
    System.err.println("  Column: " + error.column());
    System.err.println("  Value: " + error.value());
    if (error.cause() != null) {
        System.err.println("  Cause: " + error.cause().getMessage());
    }
}
```

## Supported Formats

| Format | Extension | Read | Write | Streaming Read | Streaming Write |
|--------|-----------|------|-------|----------------|-----------------|
| Excel 2007+ | `.xlsx` | ✅ | ✅ | ✅ (SAX) | ✅ (SXSSF) |
| Excel 97-2003 | `.xls` | ✅ | ✅ | ❌ (full load) | ❌ |
| CSV | `.csv` | ✅ | ✅ | ✅ (buffered) | ✅ |

## Requirements

- Java 11 or higher
- Apache POI 5.2.5
- OpenCSV 5.9

## License

Apache License 2.0 — See [LICENSE](LICENSE) for details.

## Building from Source

```bash
git clone https://github.com/chitralabs/sheetz.git
cd sheetz
mvn clean install
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed list of changes in each release.

## Security

To report a vulnerability, please see [SECURITY.md](SECURITY.md). Do not open a public issue.

