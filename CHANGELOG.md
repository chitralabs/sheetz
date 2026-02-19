# Changelog

All notable changes to Sheetz will be documented here.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Sheetz follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Nothing yet — [see roadmap](ROADMAP.md) for what's coming

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

[Unreleased]: https://github.com/chitralabs/sheetz/compare/v1.0.1...HEAD
[1.0.1]: https://github.com/chitralabs/sheetz/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/chitralabs/sheetz/releases/tag/v1.0.0
