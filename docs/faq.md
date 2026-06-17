---
layout: default
title: "Sheetz FAQ - Java Excel Library Questions & Answers"
description: "Frequently asked questions about Sheetz, the Java library for reading and writing Excel, CSV, and ODS files. Setup, migration, performance, and troubleshooting."
---

## Frequently Asked Questions

### General

**Q: What file formats does Sheetz support?**

Sheetz supports four formats: `.xlsx` (Excel 2007+), `.xls` (Excel 97-2003), `.csv`, and `.ods` (LibreOffice Calc). The API is identical for all formats — just change the file extension.

**Q: What Java versions are supported?**

Java 11 and above. Sheetz is tested on Java 11, 17, and 21 across Linux, macOS, and Windows.

**Q: Is Sheetz thread-safe?**

Yes. All static methods on `Sheetz` are fully thread-safe and can be called from multiple threads concurrently without synchronization.

**Q: Is Sheetz free for commercial use?**

Yes. Sheetz is licensed under the Apache License 2.0, which permits commercial use, modification, and distribution.

---

### Setup

**Q: How do I add Sheetz to my Maven project?**

```xml
<dependency>
    <groupId>io.github.chitralabs.sheetz</groupId>
    <artifactId>sheetz-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

**Q: How do I add Sheetz to my Gradle project?**

```groovy
implementation 'io.github.chitralabs.sheetz:sheetz-core:1.1.0'
```

**Q: Do I need Apache POI as a separate dependency?**

No. Sheetz includes Apache POI 5.2.5 as a transitive dependency. You do not need to add POI separately.

**Q: How do I enable ODS (LibreOffice) support?**

Add the optional ODF Toolkit dependency:

```xml
<dependency>
    <groupId>org.odftoolkit</groupId>
    <artifactId>odfdom-java</artifactId>
    <version>0.12.0</version>
</dependency>
```

Without this dependency, calling `Sheetz.read("file.ods", ...)` will throw a clear error message explaining what to add.

---

### Performance

**Q: How does Sheetz handle large files?**

For reads, `Sheetz.stream()` uses SAX-based parsing with constant ~10MB memory regardless of file size. For writes, Sheetz automatically switches to Apache POI's SXSSF (streaming) mode for files larger than 10,000 rows.

**Q: Is Sheetz faster than Apache POI?**

For writes at scale, yes. At 100K rows, Sheetz writes are 5.8x faster than Apache POI because of automatic SXSSF streaming. For reads, Sheetz is comparable to POI while adding annotation mapping, type conversion, and validation. See [full benchmarks]({{ '/benchmarks' | relative_url }}).

**Q: How does Sheetz compare to EasyExcel and FastExcel?**

FastExcel and EasyExcel have faster raw throughput but require significantly more code and lack features like multi-format support, built-in validation, and automatic type conversion for 19 Java types. See the [benchmarks page]({{ '/benchmarks' | relative_url }}) for detailed comparisons.

---

### Migration

**Q: Can I use Sheetz alongside Apache POI in the same project?**

Yes. Sheetz uses POI internally, so they coexist. You can migrate incrementally — use Sheetz for new code and keep existing POI code unchanged. See the [migration guide]({{ '/migration-from-poi' | relative_url }}).

**Q: Does Sheetz support all POI features?**

Sheetz covers the most common use cases: typed read/write, streaming, validation, cell styling, hyperlinks, merged cells, auto-filters, and multi-sheet workbooks. For advanced POI features like formula evaluation or chart creation, you may still need POI directly. See the [roadmap](https://github.com/chitralabs/sheetz/blob/main/ROADMAP.md) for planned features.

---

### Troubleshooting

**Q: I get "Unsupported format" for ODS files — what do I do?**

Add the ODF Toolkit dependency to your project. See the ODS setup question above.

**Q: My dates are not parsing correctly — how do I fix it?**

Use the `@Column(format = "dd/MM/yyyy")` annotation to specify the date format in your source file. Sheetz supports `LocalDate`, `LocalDateTime`, `LocalTime`, `Instant`, and `ZonedDateTime`.

**Q: How do I read from a specific sheet in a multi-sheet workbook?**

Use the builder API:

```java
List<Product> data = Sheetz.reader(Product.class)
    .file("report.xlsx")
    .sheet("Inventory")
    .read();
```

**Q: How do I style cells when writing?**

Use the `@Style` annotation on your model fields:

```java
public class StyledProduct {
    @Column("Product Name")
    @Style(bold = true, fontColor = "#0000FF")
    public String name;

    @Style(backgroundColor = "#FFFF00", dataFormat = "#,##0.00")
    public Double price;
}
```

Or use the programmatic `CellStyleBuilder`:

```java
CellStyleDef style = CellStyleBuilder.create()
    .bold(true)
    .backgroundColor("#003366")
    .fontColor("#FFFFFF")
    .build();

Sheetz.writer(Product.class)
    .data(products)
    .file("styled.xlsx")
    .headerStyle(style)
    .write();
```
