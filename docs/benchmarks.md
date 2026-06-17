---
layout: default
title: "Sheetz Performance Benchmarks - Java Excel Library Comparison"
description: "JMH benchmark results comparing Sheetz vs Apache POI, EasyExcel, FastExcel, and Poiji. Write performance, read performance, and memory usage across 1K-100K rows."
---

## Performance Benchmarks

Reproducible [JMH](https://openjdk.org/projects/code-tools/jmh/) benchmarks comparing Sheetz against Apache POI, EasyExcel, FastExcel, and Poiji.

All source code: [sheetz-benchmarks](https://github.com/chitralabs/sheetz-benchmarks)

---

### Write Performance (ms/op — lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 6.48 | 31.95 | 309.70 |
| EasyExcel | 11.44 | 58.60 | 542.84 |
| **Sheetz** | **23.15** | **232.51** | **423.75** |
| Apache POI | 22.46 | 217.17 | 2,453.35 |

At small sizes (1K-10K rows), Sheetz and Apache POI are similar. **At 100K rows, Sheetz is 5.8x faster** because it automatically switches to SXSSF streaming.

```
Write Performance — 100K rows (ms/op, lower is better)

FastExcel   ||||||||                                309ms
Sheetz      ||||||||||                              423ms  <-- auto SXSSF
EasyExcel   |||||||||||||                           542ms
Apache POI  ||||||||||||||||||||||||||||||||||||||  2453ms
```

---

### Read Performance (ms/op — lower is better)

| Library | 1K rows | 10K rows | 100K rows |
|---------|--------:|---------:|----------:|
| FastExcel | 2.43 | 24.88 | 210.17 |
| EasyExcel | 4.91 | 42.66 | 334.17 |
| Apache POI | 10.86 | 106.02 | 1,097.20 |
| Poiji | 12.26 | 114.92 | 1,042.16 |
| **Sheetz** | **13.18** | **128.35** | **1,285.89** |

Sheetz read speed is comparable to Apache POI and Poiji. The difference from FastExcel/EasyExcel is the cost of automatic type conversion, annotation processing, and validation — features those libraries do not provide.

---

### Feature vs Performance Trade-off

| Library | Code Lines (R+W) | Auto Types | Validation | Multi-Format |
|---------|:-:|:-:|:-:|:-:|
| **Sheetz** | **2** | 19 types | Built-in | xlsx/xls/csv/ods |
| Apache POI | ~45 | None | None | xlsx/xls |
| EasyExcel | ~15 | Basic | None | xlsx only |
| FastExcel | ~33 | None | None | xlsx only |
| Poiji | ~1 (read) | Basic | None | xlsx only |

---

### Methodology

| Setting | Value |
|---------|-------|
| Framework | JMH 1.37 |
| JVM | JDK 11.0.30, OpenJDK 64-Bit Server VM |
| Hardware | Apple Silicon (macOS) |
| Forks | 2 separate JVM processes |
| Warmup | 3 iterations, 1s each |
| Measurement | 5 iterations, 1s each |
| Row counts | 1,000 / 10,000 / 100,000 |
| Data | Fixed random seed for reproducibility |
| Format | .xlsx (OOXML) for all libraries |

Results vary by JVM, OS, and hardware. [Run the benchmarks yourself](https://github.com/chitralabs/sheetz-benchmarks#how-to-run) to get numbers for your environment.

---

### When to Choose Each Library

- **Need minimal code + all features?** — Sheetz (1-line API, validation, 19 converters, 4 formats)
- **Need maximum raw throughput?** — FastExcel (fastest reads and writes)
- **Already using POI?** — Sheetz wraps POI, so it is a drop-in for new code
- **Processing 1M+ rows, memory critical?** — `Sheetz.stream()` (~10MB constant) or FastExcel
- **Need annotations + fast reads?** — EasyExcel (but requires listener pattern)
