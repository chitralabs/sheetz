# Sheetz v1.0.1 — Initial Release

We're excited to release the first stable version of **Sheetz** — a lightweight Java library
that lets you read, write, and stream Excel (.xlsx/.xls) and CSV files with a single line of code.

## What's New

### One-Liner API
```java
// Read any Excel or CSV file into typed Java objects
List<Product> products = Sheetz.read("data.xlsx", Product.class);

// Write back in one line
Sheetz.write(products, "output.xlsx");
```

### Key Features
- **Zero boilerplate** — no Workbook, Sheet, Row, or Cell objects to manage
- **True SAX streaming** — process million-row files with constant ~10MB memory
- **19 built-in type converters** — LocalDate, BigDecimal, Enum, UUID, Boolean, and more
- **Annotation-based mapping** — `@Column` for headers, required fields, defaults, custom formats
- **Built-in validation** — per-row error reporting with column names and row numbers
- **Thread-safe** — safe for concurrent use in multi-threaded applications
- **Multi-format** — XLSX, XLS (legacy), and CSV from one unified API

### Performance vs Apache POI (100K rows)
| Operation | Sheetz | Apache POI | Improvement |
|-----------|--------|------------|-------------|
| Write | 423ms | 2,453ms | **5.8x faster** |
| Memory | ~10MB | ~340MB | **34x less memory** |
| Lines of code | 1 | 45+ | **45x less code** |

## Maven

```xml
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Resources
- [Documentation & Examples](https://github.com/chitralabs/sheetz-examples)
- [Performance Benchmarks](https://github.com/chitralabs/sheetz-benchmarks)
- [Maven Central](https://central.sonatype.com/artifact/io.github.chitralabs.sheetz/sheetz-core)

## Contributors
Thanks to everyone who tested and provided feedback during development.

---
*If Sheetz saved you time, please give us a star — it helps other Java developers find this project.*
