---
layout: default
title: "Migrate from Apache POI to Sheetz - Java Excel Migration Guide"
description: "Step-by-step guide to migrate from Apache POI to Sheetz. Replace 45+ lines of boilerplate with 1-line API calls. Side-by-side code comparison."
---

## Migrate from Apache POI to Sheetz

This guide shows how to replace Apache POI code with Sheetz, step by step.

### Why Migrate?

| | Apache POI | Sheetz |
|---|---|---|
| **Lines to read Excel** | 20+ | **1** |
| **Lines to write Excel** | 25+ | **1** |
| **Write 100K rows** | 2,453ms | **423ms (5.8x faster)** |
| **Streaming memory** | ~340MB (manual config) | **~10MB (automatic)** |
| **Type conversion** | Manual casting | **19 auto converters** |
| **Validation** | DIY | **Built-in** |
| **ODS support** | Limited | **Full read/write** |
| **Learning curve** | Workbook > Sheet > Row > Cell | **One method call** |

---

### Step 1: Replace the Dependency

Remove Apache POI:

```xml
<!-- REMOVE these -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
</dependency>
```

Add Sheetz:

```xml
<!-- ADD this -->
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

> **Note:** Sheetz uses Apache POI internally. You do not need POI as a direct dependency.

---

### Step 2: Replace Read Code

**Before (Apache POI — 15 lines):**

```java
List<Product> products = new ArrayList<>();
try (Workbook workbook = new XSSFWorkbook(new FileInputStream("products.xlsx"))) {
    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;
        Product p = new Product();
        p.name = row.getCell(0).getStringCellValue();
        p.price = row.getCell(1).getNumericCellValue();
        p.inStock = row.getCell(2).getBooleanCellValue();
        Cell dateCell = row.getCell(3);
        if (dateCell != null) {
            p.releaseDate = dateCell.getLocalDateTimeCellValue().toLocalDate();
        }
        products.add(p);
    }
}
```

**After (Sheetz — 1 line):**

```java
List<Product> products = Sheetz.read("products.xlsx", Product.class);
```

---

### Step 3: Replace Write Code

**Before (Apache POI — 25 lines):**

```java
try (Workbook workbook = new XSSFWorkbook()) {
    Sheet sheet = workbook.createSheet("Products");
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("name");
    headerRow.createCell(1).setCellValue("price");
    headerRow.createCell(2).setCellValue("inStock");
    headerRow.createCell(3).setCellValue("releaseDate");
    for (int i = 0; i < products.size(); i++) {
        Row row = sheet.createRow(i + 1);
        Product p = products.get(i);
        row.createCell(0).setCellValue(p.name);
        row.createCell(1).setCellValue(p.price);
        row.createCell(2).setCellValue(p.inStock);
        if (p.releaseDate != null) {
            Cell cell = row.createCell(3);
            cell.setCellValue(p.releaseDate);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(
                workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            cell.setCellStyle(dateStyle);
        }
    }
    try (FileOutputStream fos = new FileOutputStream("output.xlsx")) {
        workbook.write(fos);
    }
}
```

**After (Sheetz — 1 line):**

```java
Sheetz.write(products, "output.xlsx");
```

---

### Step 4: Replace Streaming Code

**Before (Apache POI SXSSF — manual configuration):**

```java
try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
    Sheet sheet = workbook.createSheet("Products");
    // ... same manual cell-by-cell code ...
    workbook.write(new FileOutputStream("huge.xlsx"));
    workbook.dispose(); // Clean up temp files
}
```

**After (Sheetz — automatic streaming):**

```java
// Sheetz automatically uses SXSSF for files > 10K rows
Sheetz.write(products, "huge.xlsx");

// Or stream reads with constant ~10MB memory:
try (StreamingReader<Product> reader = Sheetz.stream("huge.xlsx", Product.class)) {
    for (Product p : reader) {
        database.save(p);
    }
}
```

---

### Step 5: Add Validation (New Capability)

Apache POI has no built-in validation. With Sheetz:

```java
ValidationResult<Product> result = Sheetz.validate("data.xlsx", Product.class);
result.errors().forEach(e ->
    System.out.printf("Row %d [%s]: %s%n", e.row(), e.column(), e.message()));
List<Product> validOnly = result.validRows();
```

---

### Annotation Mapping Reference

Replace manual cell-index mapping with annotations:

```java
public class Product {
    @Column("Product Name")        // Map to header text
    public String name;

    @Column(index = 1)             // Map by column index
    public Double price;

    @Column(required = true)       // Fail validation if empty
    public Boolean inStock;

    @Column(format = "dd/MM/yyyy") // Custom date format
    public LocalDate releaseDate;
}
```

---

### Compatibility Notes

- **Java version**: Sheetz requires Java 11+. If you are on Java 8, you must upgrade first.
- **POI interop**: Sheetz uses Apache POI 5.2.5 internally. If other parts of your codebase use POI directly, verify version compatibility.
- **File formats**: Sheetz supports `.xlsx`, `.xls`, `.csv`, and `.ods`. The API is identical for all formats.
- **Thread safety**: Unlike POI's Workbook objects, Sheetz static methods are fully thread-safe.

---

### Need Help Migrating?

- [Full API documentation]({{ '/' | relative_url }})
- [9 runnable examples](https://github.com/chitralabs/sheetz-examples)
- [Ask a question](https://github.com/chitralabs/sheetz/discussions)
