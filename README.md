# Sheetz

[![Build](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml/badge.svg)](https://github.com/chitralabs/sheetz/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.chitralabs.sheetz/sheetz-core)](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/chitralabs/sheetz?style=social)](https://github.com/chitralabs/sheetz/stargazers)

> **Read and write Excel & CSV files in Java with a single line of code.**

```java
// The entire API, right here:
List<Product> products = Sheetz.read("data.xlsx", Product.class);
Sheetz.write(products, "output.xlsx");
```

No Workbook objects. No Cell iteration. No boilerplate. Just data.

---

## ⚡ Why Sheetz?

Apache POI requires **45+ lines** to do what Sheetz does in **1**. Here's the proof:

| | Sheetz | Apache POI | EasyExcel | FastExcel |
|---|---|---|---|---|
| **Lines to read 100K rows** | **1** | 20+ | 12+ (listener) | 15+ |
| **Lines to write 100K rows** | **1** | 25+ | 3 | 18+ |
| **Write speed (100K rows)** | 423ms | 2,453ms | 542ms | 309ms |
| **Memory (streaming)** | **~10MB** | ~340MB | ~85MB | ~40MB |
| **Annotation mapping** | ✅ | ❌ | ✅ | ❌ |
| **Built-in validation** | ✅ | ❌ | ❌ | ❌ |
| **Auto type conversion** | ✅ 19 types | ❌ | ⚠️ basic | ❌ |
| **Multi-format (xlsx/xls/csv)** | ✅ | ✅ | ⚠️ xlsx only | ⚠️ xlsx only |

📊 [Full JMH benchmark results & methodology →](https://github.com/chitralabs/sheetz-benchmarks)

---

## 🚀 Quick Start

### Add dependency

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

### Define your model

```java
public class Product {
    public String name;
    public Double price;
    public Boolean inStock;
    public LocalDate releaseDate;

    public Product() {} // Required no-arg constructor
}
```

### Read → Process → Write

```java
// Read from Excel
List<Product> products = Sheetz.read("products.xlsx", Product.class);

// Read from CSV
List<Product> products = Sheetz.read("products.csv", Product.class);

// Write to Excel
Sheetz.write(products, "output.xlsx");

// Stream 1M rows with ~10MB memory
Sheetz.stream("huge.xlsx", Product.class)
      .forEach(product -> process(product));
```

⭐ **If this just saved you time, please star this repo** — it helps other Java developers find Sheetz.

---

## ✨ Features

- 🚀 **One-liner API** — `Sheetz.read()`, `Sheetz.write()`, `Sheetz.stream()`, `Sheetz.validate()`
- 📊 **True SAX Streaming** — constant ~10MB memory for files of any size
- 🔄 **19 Auto Converters** — LocalDate, LocalDateTime, BigDecimal, UUID, Enum, Boolean (`yes/true/1/on`), and more
- 🧵 **Thread-Safe** — safe for concurrent use without synchronization
- ✅ **Row-Level Validation** — per-row errors with column name, value, and root cause
- 📝 **`@Column` Annotation** — map to any header name, index, required, default, date format, custom converter
- 📑 **Multi-Sheet Workbook** — write different model types to different sheets in one call
- 📁 **Multi-Format** — XLSX, XLS (legacy 97-2003), and CSV from one unified API
- ⚙️ **Builder API** — fine-grained control via `Sheetz.reader()` and `Sheetz.writer()` builders
- 🔌 **Extensible** — implement `Converter<T>` interface for custom types

---

## 📖 Usage Examples

### Annotation Mapping

```java
public class Invoice {
    @Column("Invoice #")              // Custom header name
    public String invoiceNumber;

    @Column(index = 1)                // Map by column index
    public Double amount;

    @Column(required = true)          // Fail validation if empty
    public String customerId;

    @Column(defaultValue = "pending") // Default for empty cells
    public String status;

    @Column(format = "dd/MM/yyyy")    // Custom date format
    public LocalDate dueDate;

    @Column(converter = MoneyConverter.class) // Custom converter
    public BigDecimal total;

    @Column(ignore = true)            // Skip this field
    public String internalId;
}
```

### Streaming Large Files

```java
// Row-by-row — constant memory regardless of file size
try (StreamingReader<Product> reader = Sheetz.stream("huge.xlsx", Product.class)) {
    for (Product p : reader) {
        database.save(p);
    }
}

// Batch processing — 1000 rows at a time
Sheetz.stream("huge.xlsx", Product.class)
      .batch(1000)
      .forEach(batch -> database.bulkInsert(batch));

// Java Streams integration
long expensiveCount = Sheetz.stream("products.xlsx", Product.class)
      .stream()
      .filter(p -> p.price > 1000)
      .count();
```

### Validation

```java
ValidationResult<Product> result = Sheetz.validate("data.xlsx", Product.class);

System.out.printf("Valid: %d | Errors: %d | Rate: %.1f%%%n",
    result.validCount(), result.errorCount(), result.successRate());

result.errors().forEach(error ->
    System.out.printf("Row %d [%s]: %s%n",
        error.row(), error.column(), error.message()));

List<Product> validOnly = result.validRows();
```

### Multi-Sheet Workbook

```java
Sheetz.workbook()
      .sheet("Products", products)
      .sheet("Employees", employees)
      .sheet("Orders", orders)
      .write("monthly-report.xlsx");
```

### Builder API

```java
// Fine-grained reader control
List<Product> data = Sheetz.reader(Product.class)
    .file("report.xlsx")
    .sheet("Inventory")
    .headerRow(1)
    .read();

// Fine-grained writer control
Sheetz.writer(Product.class)
    .data(products)
    .file("output.xlsx")
    .sheet("Products")
    .autoSize(true)
    .freezeHeader(true)
    .write();
```

---

## 📊 Supported Types

| Type | Example Input | Notes |
|------|--------------|-------|
| `String` | Any text | Trimmed by default |
| `Integer` / `Long` / `Double` | `42`, `3.14` | Standard numeric |
| `BigDecimal` | `99.99` | Precise decimals |
| `Boolean` | `true`, `yes`, `y`, `1`, `on` | Case-insensitive |
| `LocalDate` | `2024-01-15` | Configurable format |
| `LocalDateTime` | `2024-01-15 10:30:00` | Configurable format |
| `LocalTime` | `10:30:00` | ISO format |
| `Instant` | `2024-01-15T10:30:00Z` | UTC |
| `ZonedDateTime` | `2024-01-15T10:30:00+05:30` | With timezone |
| `UUID` | `550e8400-...` | Standard UUID |
| `Enum` | `ACTIVE`, `active` | Case-insensitive |
| Custom | Anything | Implement `Converter<T>` |

---

## 🗺️ Roadmap

Contributions welcome for any of these! See [CONTRIBUTING.md](CONTRIBUTING.md).

- [ ] ODS (LibreOffice Calc) format support — [#help-wanted]
- [ ] Async/reactive streaming API (`Sheetz.streamAsync()`)
- [ ] Google Sheets native reader via API
- [ ] Excel formula write support
- [ ] Password-protected file support
- [ ] Spring Boot auto-configuration starter
- [ ] Quarkus extension
- [ ] Excel chart generation API

[Full roadmap and ideas →](https://github.com/chitralabs/sheetz/discussions)

---

## 🏢 Used By

Are you using Sheetz in production? [Open a PR to add your project here](CONTRIBUTING.md) —
it helps other developers discover the library.

*Be the first!* 🚀

---

## 🤝 Contributing

Contributions are very welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md).

Looking for a good first issue? [Browse issues labelled `good first issue`](https://github.com/chitralabs/sheetz/issues?q=label%3A%22good+first+issue%22).

---

## 📚 More Resources

| Resource | Description |
|----------|-------------|
| [sheetz-examples](https://github.com/chitralabs/sheetz-examples) | 8 runnable demos covering every feature |
| [sheetz-benchmarks](https://github.com/chitralabs/sheetz-benchmarks) | JMH benchmarks vs Apache POI, EasyExcel, FastExcel, Poiji |
| [Maven Central](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core) | Latest release |
| [GitHub Discussions](https://github.com/chitralabs/sheetz/discussions) | Q&A and community |
| [Issues](https://github.com/chitralabs/sheetz/issues) | Bug reports and feature requests |

---

## 📄 License

[Apache License 2.0](LICENSE) — free for commercial and personal use.

---

*Sheetz is built with ❤️ by [chitralabs](https://github.com/chitralabs)*
