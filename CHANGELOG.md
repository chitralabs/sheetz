# Changelog

All notable changes to Sheetz will be documented here.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Sheetz follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

---

## [1.1.0] — 2026-06-16

### Added

- **Cell Formatting API** — New `@Style` annotation for controlling cell visual presentation (fonts, colors, borders, alignment, text wrapping, data formats).
- **`io.github.chitralabs.sheetz.style` package** with `CellStyleDef` (immutable value object), `CellStyleBuilder` (fluent builder), `StyleAnnotationParser`, and `PoiStyleResolver` (POI style conversion with per-workbook caching).
- **Hyperlink support** — `HyperlinkValue` type for fields that hold both display text and URL; `@Style(hyperlink = true)` for string fields containing URLs.
- **Merged cell read support** — `MergedRegionResolver` pre-computes (row, col) to master cell lookup for O(1) merged cell reads in Excel files.
- **Auto-filter support** — `ExcelWriter.autoFilter(boolean)` and `WriterBuilder.autoFilter(boolean)` to add filter dropdowns on the header row.
- **Merge region support** — `ExcelWriter.mergeRegion()` and `WriterBuilder.mergeRegion()` for programmatic cell merging during writes.
- **Custom header styles** — `ExcelWriter.headerStyle(CellStyleDef)` and `WriterBuilder.headerStyle(CellStyleDef)` for overriding the default header style.
- **ODS (LibreOffice Calc) format support** — Read and write `.ods` files using ODF Toolkit (`org.odftoolkit:odfdom-java:0.12.0`), added as an optional dependency.
  - `OdsReader` — Reads ODS files with full support for typed reads, readMaps, readRaw, and validation.
  - `OdsWriter` — Writes ODS files with annotation-based mapping.
  - `OdsWriteSupport` — Shared ODS cell value writing utilities.
  - `WorkbookBuilder` — Multi-sheet ODS write support.
  - `StreamingReader` — ODS fallback to in-memory read (no SAX streaming API for ODS).
  - Runtime safety: clear `SheetzException` message when ODFDOM library is absent.
- `Format.ODS` enum value with `isOds()` and `isSpreadsheet()` methods.
- `Format.detect()` now recognizes `.ods` extension.
- `HyperlinkValue` converter registered in `Converters` for automatic type handling.

### Changed

- Updated `maven-surefire-plugin` from 3.5.4 to 3.5.5.
- Updated `jacoco-maven-plugin` from 0.8.11 to 0.8.14.
- Updated `junit-jupiter` from 5.10.1 to 5.14.4.
- Updated `slf4j` from 2.0.17 to 2.0.18.
- Updated `actions/upload-artifact` from v6 to v7 in CI workflow.
- `ExcelWriteSupport.writeRow()` now accepts an optional `PoiStyleResolver` for per-column style application.
- `ExcelWriter` and `WorkbookBuilder` now instantiate `PoiStyleResolver` for style-aware writes.
- `ExcelReader` now resolves merged region values and extracts hyperlinks from cells.
- `FieldMapping` now parses `@Style` annotations and exposes `styleDef()` and `hasStyle()`.
- `Sheetz` facade updated with three-way format dispatch (CSV/Excel/ODS) in all read/write/stream/validate methods.
- `module-info.java` exports `io.github.chitralabs.sheetz.style` package and adds `requires static odfdom.java` and `requires java.desktop`.

---

## [1.0.2] — 2026-02-19

### Fixed
- Fix Windows file locking issue in `StreamingReader.close()` — parser thread now joins before returning, ensuring file handles are fully released before temp directory cleanup
- Upgraded OpenCSV from 5.9 to 5.12.0
- Upgraded AssertJ from 3.24.2 to 3.27.7

### Changed
- Moved OWASP dependency-check to a separate weekly security scan workflow for faster CI
- Upgraded maven-gpg-plugin from 3.1.0 to 3.2.8
- Upgraded maven-javadoc-plugin from 3.6.3 to 3.12.0
- Upgraded maven-surefire-plugin from 3.2.2 to 3.5.4
- Upgraded maven-jar-plugin from 3.3.0 to 3.5.0
- Upgraded maven-source-plugin from 3.3.0 to 3.4.0
- Upgraded maven-compiler-plugin from 3.11.0 to 3.15.0
- Upgraded SLF4J from 2.0.9 to 2.0.17
- Upgraded GitHub Actions (checkout v6, setup-java v5, upload-artifact v6, codecov v5)

---

## [1.0.1] — 2026-02-13

### Fixed
- Internal dependency alignment and build reproducibility improvements

---

## [1.0.0] — 2026-02-10

### Added
- `Sheetz.read()` — one-line Excel/CSV to typed Java objects
- `Sheetz.write()` — one-line Java objects to Excel/CSV
- `Sheetz.stream()` — SAX-based streaming with constant ~10MB memory
- `Sheetz.validate()` — row-level validation with per-column error details
- `Sheetz.readMaps()` — schema-free reading to `List<Map<String, Object>>`
- `Sheetz.readRaw()` — reading to `List<String[]>` for raw string access
- `@Column` annotation — header name, index, required, default, format, converter, ignore, width
- 19 built-in type converters: String, Integer, Long, Double, Float, Short, Byte, Character,
  BigDecimal, BigInteger, Boolean, LocalDate, LocalDateTime, LocalTime, Instant, ZonedDateTime,
  java.util.Date (legacy), UUID, Enum
- `Converter<T>` interface for custom type converters
- `SheetzConfig` — global configuration for date formats, encoding, streaming threshold
- Builder API: `Sheetz.reader()` and `Sheetz.writer()` for fine-grained control
- `Sheetz.workbook()` — multi-sheet workbook builder
- `Sheetz.register()` — global converter registration
- Thread-safety for all read/write operations
- Apache POI 5.2.5 integration (XLSX/XLS)
- OpenCSV 5.9 integration (CSV)
- SXSSF automatic streaming for large write operations (>10K rows)
- Java 11+ compatibility

### Performance (vs Apache POI, 100K rows)
- Write: **5.8x faster** (423ms vs 2,453ms)
- Memory: **34x less** (~10MB vs ~340MB)

---

[Unreleased]: https://github.com/chitralabs/sheetz/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/chitralabs/sheetz/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/chitralabs/sheetz/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/chitralabs/sheetz/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/chitralabs/sheetz/releases/tag/v1.0.0
