---
layout: default
title: Sheetz - Java Excel, CSV & ODS Library
---

## One-liner API for Spreadsheets

```java
List<Product> products = Sheetz.read("data.xlsx", Product.class);
Sheetz.write(products, "output.xlsx");
```

No Workbook objects. No Cell iteration. No boilerplate. Just data.

---

## Quick Start

### Maven

```xml
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.chitralabs.sheetz:sheetz-core:1.1.0'
```

---

## Features

| Feature | Description |
|---------|-------------|
| **One-liner API** | `Sheetz.read()`, `Sheetz.write()`, `Sheetz.stream()`, `Sheetz.validate()` |
| **True SAX Streaming** | Constant ~10MB memory for files of any size |
| **19 Auto Converters** | LocalDate, BigDecimal, UUID, Enum, Boolean, and more |
| **Thread-Safe** | Safe for concurrent use without synchronization |
| **Row-Level Validation** | Per-row errors with column name, value, and root cause |
| **@Style Annotation** | Fonts, colors, borders, alignment, hyperlinks, merged cells |
| **@Column Annotation** | Map to any header name, index, required, default, format |
| **Multi-Sheet Workbook** | Write different model types to different sheets |
| **Multi-Format** | XLSX, XLS, CSV, and ODS from one unified API |
| **Extensible** | Implement `Converter<T>` for custom types |

---

## Read & Write

```java
// Read from any format
List<Product> products = Sheetz.read("products.xlsx", Product.class);
List<Product> csvData  = Sheetz.read("products.csv", Product.class);
List<Product> odsData  = Sheetz.read("products.ods", Product.class);

// Write to any format
Sheetz.write(products, "output.xlsx");
Sheetz.write(products, "output.csv");
Sheetz.write(products, "output.ods");
```

---

## Annotation Mapping

```java
public class Invoice {
    @Column("Invoice #")
    public String invoiceNumber;

    @Column(required = true)
    public String customerId;

    @Column(defaultValue = "pending")
    public String status;

    @Column(format = "dd/MM/yyyy")
    public LocalDate dueDate;

    @Column(converter = MoneyConverter.class)
    public BigDecimal total;
}
```

---

## Streaming Large Files

Process million-row files with constant ~10MB memory:

```java
try (StreamingReader<Product> reader = Sheetz.stream("huge.xlsx", Product.class)) {
    for (Product p : reader) {
        database.save(p);
    }
}

// Batch processing
Sheetz.stream("huge.xlsx", Product.class)
      .batch(1000)
      .forEach(batch -> database.bulkInsert(batch));
```

---

## Cell Styling

### @Style Annotation

```java
public class StyledProduct {
    @Column("Product Name")
    @Style(bold = true, fontColor = "#0000FF")
    public String name;

    @Style(backgroundColor = "#FFFF00", horizontalAlignment = "CENTER")
    public Double price;

    @Style(hyperlink = true)
    public HyperlinkValue website;
}
```

### Programmatic Styles

```java
CellStyleDef headerStyle = CellStyleBuilder.create()
    .bold(true)
    .backgroundColor("#003366")
    .fontColor("#FFFFFF")
    .horizontalAlignment("CENTER")
    .build();

Sheetz.writer(Product.class)
    .data(products)
    .file("styled.xlsx")
    .headerStyle(headerStyle)
    .autoFilter(true)
    .write();
```

---

## Validation

```java
ValidationResult<Product> result = Sheetz.validate("data.xlsx", Product.class);

System.out.printf("Valid: %d | Errors: %d | Rate: %.1f%%%n",
    result.validCount(), result.errorCount(), result.successRate());

result.errors().forEach(error ->
    System.out.printf("Row %d [%s]: %s%n",
        error.row(), error.column(), error.message()));
```

---

## Multi-Sheet Workbook

```java
Sheetz.workbook()
      .sheet("Products", products)
      .sheet("Employees", employees)
      .sheet("Orders", orders)
      .write("monthly-report.xlsx");
```

---

## Supported Types

| Type | Example | Notes |
|------|---------|-------|
| `String` | Any text | Trimmed by default |
| `Integer` / `Long` / `Double` | `42`, `3.14` | Standard numeric |
| `BigDecimal` | `99.99` | Precise decimals |
| `Boolean` | `true`, `yes`, `1`, `on` | Case-insensitive |
| `LocalDate` | `2024-01-15` | Configurable format |
| `LocalDateTime` | `2024-01-15 10:30:00` | Configurable format |
| `UUID` | `550e8400-...` | Standard UUID |
| `Enum` | `ACTIVE` | Case-insensitive |
| Custom | Anything | Implement `Converter<T>` |

---

## ODS Support

Add the optional dependency for LibreOffice `.ods` files:

```xml
<dependency>
    <groupId>org.odftoolkit</groupId>
    <artifactId>odfdom-java</artifactId>
    <version>0.12.0</version>
</dependency>
```

```java
List<Product> products = Sheetz.read("products.ods", Product.class);
Sheetz.write(products, "output.ods");
```

---

## Resources

| Resource | Link |
|----------|------|
| GitHub | [chitralabs/sheetz](https://github.com/chitralabs/sheetz) |
| Maven Central | [sheetz-core](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core) |
| Examples | [sheetz-examples](https://github.com/chitralabs/sheetz-examples) |
| Benchmarks | [sheetz-benchmarks](https://github.com/chitralabs/sheetz-benchmarks) |
| Spring Boot Starter | [sheetz-spring-boot-starter](https://github.com/chitralabs/sheetz-spring-boot-starter) |

---

Apache License 2.0 | Built by [chitralabs](https://github.com/chitralabs)
