# Sheetz Roadmap

This document outlines planned features and improvements. All items marked with
🙏 **contributions welcome** are open for community PRs.

---

## v1.2.0 — Integrations & Formats
*Target: Q2 2026*

| Feature | Status | Difficulty | Notes |
|---------|--------|------------|-------|
| ODS (LibreOffice) format support | 🙏 Open | Medium | Apache POI supports HSSF/XSSF; needs ODS equivalent |
| Spring Boot Auto-Configuration Starter | 🙏 Open | Medium | New repo: `sheetz-spring-boot-starter` |
| Quarkus Extension | 🙏 Open | Medium | New repo: `sheetz-quarkus-extension` |
| Async streaming API (`Sheetz.streamAsync()`) | 🙏 Open | Hard | Returns `CompletableFuture<Stream<T>>` |

## v1.3.0 — Advanced Excel Features
*Target: Q3 2026*

| Feature | Status | Difficulty | Notes |
|---------|--------|------------|-------|
| Excel formula write support | 🙏 Open | Hard | Write cells with formulas |
| Password-protected file support | 🙏 Open | Medium | Read/write encrypted .xlsx |
| Conditional formatting API | 🙏 Open | Hard | Apply cell styles based on values |
| Freeze panes (rows AND columns) | 🙏 Open | Easy | Extend existing freezeHeader support |

## v2.0.0 — Next Generation API
*Target: 2027 — design proposals welcome*

| Feature | Status | Difficulty | Notes |
|---------|--------|------------|-------|
| Google Sheets native reader | Planned | Hard | OAuth2 + Sheets API v4 integration |
| Pandas-style DataFrame API | Exploring | Very Hard | `Sheetz.dataframe("data.xlsx")` |
| Parquet format support | Exploring | Hard | For data engineering use cases |
| Excel chart generation | Exploring | Hard | Create charts from data programmatically |

---

## How to Contribute to the Roadmap

1. Pick any item marked 🙏 and open an issue to claim it
2. Or propose new roadmap items via [GitHub Discussions](https://github.com/chitralabs/sheetz/discussions)
3. Large features should start with a design proposal in Discussions before implementation

---

## Completed

| Feature | Version | PR |
|---------|---------|-----|
| Annotation-based column mapping (`@Column`) | v1.0.0 | — |
| SAX streaming for large files | v1.0.0 | — |
| 19 built-in type converters | v1.0.0 | — |
| Built-in row-level validation | v1.0.0 | — |
| Multi-sheet workbook write | v1.0.0 | — |
| Builder API (reader/writer) | v1.0.0 | — |
| Thread-safety | v1.0.0 | — |
