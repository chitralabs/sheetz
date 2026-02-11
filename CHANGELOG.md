# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-02-08

### Fixed

- Infinite recursion (`StackOverflowError`) in `Sheetz.validate()` for CSV files by adding `CsvReader.validate()`.
- `Instant` converter precision mismatch when round-tripping through `java.util.Date` (millisecond truncation).
- `NullPointerException` in enum converter registration test.
- Broken `ReaderBuilder.validate()` delegation introduced during merge.

### Changed

- Downgraded all Java 17+ syntax (switch expressions, pattern matching `instanceof`, records) to Java 11 equivalents for broader compatibility.
- Updated compiler source/target to Java 11 in `pom.xml`.
- CI build matrix now tests on Java 11, 17, and 21.
- Publish workflow updated to use JDK 11.
- Upgraded `central-publishing-maven-plugin` from 0.9.0 to 0.10.0.
- Added developer details and `distributionManagement` to POM for Maven Central compliance.
- Updated all documentation references from Java 17 to Java 11.

## [1.0.0] - 2025-02-08

### Added

- One-liner API through the `Sheetz` facade class for reading, writing, streaming, and validating spreadsheet files.
- Support for three file formats: XLSX (Excel 2007+), XLS (Excel 97-2003), and CSV.
- Annotation-based field mapping via `@Column` with support for custom header names, explicit column indices, required fields, default values, date format patterns, custom converters, ignored fields, and column width control.
- Nineteen built-in type converters covering String, all primitive wrappers (Integer, Long, Double, Float, Short, Byte, Character, Boolean), BigDecimal, BigInteger, LocalDate, LocalDateTime, LocalTime, Instant, ZonedDateTime, legacy Date, UUID, and case-insensitive Enum matching.
- True SAX-based streaming reader for XLSX files with constant ~10MB memory footprint regardless of file size, including configurable batch processing.
- Memory-efficient SXSSF streaming writer that kicks in automatically when row counts exceed the configurable threshold.
- Fluent builder APIs for both reading and writing, exposing sheet selection, header row offset, delimiter customisation, auto-sizing, header freezing, and streaming mode toggle.
- Multi-sheet workbook builder for producing Excel files with several typed sheets in a single call.
- Three-tier fuzzy column name resolution (exact, case-insensitive, normalised) so that model field names like `productName` match headers such as `Product Name` or `product_name` without annotation.
- Thread-safe global configuration via `SheetzConfig` stored in an `AtomicReference`, with sensible defaults for date formats, trimming, empty-row skipping, formula evaluation, and streaming thresholds.
- Detailed validation through `ValidationResult` with per-row error reporting including row number, column name, offending value, and root cause.
- Custom converter registration at both the global level (`Sheetz.register()`) and the field level (`@Column(converter = ...)`).
- Secure XML parsing with XXE protection (external entities disabled, doctype declarations disallowed) to guard against malicious XLSX files.
- Full Java Platform Module System support with a proper `module-info.java`.
- JaCoCo code coverage reporting and CI pipelines for Java 11 and 21.
- Maven Central publishing workflow with GPG signing and Sonatype Nexus staging.
- Configurable character encoding for CSV operations (defaults to UTF-8).
- Shared `ExcelWriteSupport` utility to keep cell-writing and style-creation logic in one place.

[1.0.1]: https://github.com/chitralabs/sheetz/releases/tag/v1.0.1
[1.0.0]: https://github.com/chitralabs/sheetz/releases/tag/v1.0.0
