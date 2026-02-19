---
name: 🐛 Bug Report
about: Something isn't working correctly
title: '[BUG] '
labels: bug, needs-triage
assignees: ''
---

## Bug Description
A clear, concise description of what the bug is.

## Reproduction

```java
// Minimal code that reproduces the bug
List<Product> products = Sheetz.read("data.xlsx", Product.class);
// What happens vs what you expected?
```

## Sample File
If possible, attach or describe the Excel/CSV file structure that triggers the bug.

## Expected Behavior
What you expected to happen.

## Actual Behavior
What actually happened. Include the full stack trace if there is one:

```
paste stack trace here
```

## Environment
- **Sheetz version:**
- **Java version:** (e.g. OpenJDK 17.0.5)
- **OS:** (e.g. Ubuntu 22.04 / Windows 11 / macOS 14)
- **File type:** `.xlsx` / `.xls` / `.csv`
- **File size / row count (approx):**
- **Build tool:** Maven / Gradle

## Additional Context
Any other context about the problem (e.g. only happens with certain data values, only on large files, etc.)
