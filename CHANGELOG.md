# Changelog

All notable changes to Sheetz will be documented here.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Sheetz follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Nothing yet — [see roadmap](ROADMAP.md) for what's coming

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

[Unreleased]: https://github.com/chitralabs/sheetz/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/chitralabs/sheetz/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/chitralabs/sheetz/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/chitralabs/sheetz/releases/tag/v1.0.0
