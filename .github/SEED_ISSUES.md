# Seed Issues to Create on GitHub

Create each of these as real GitHub Issues with the specified labels.
These are REAL feature opportunities, not fake issues.

---

### Issue 1
**Title:** Add ODS (LibreOffice Calc) format support
**Labels:** enhancement, good first issue, help wanted
**Body:**
## Feature Request: ODS Format Support

Many enterprise users on Linux use LibreOffice Calc and work with .ods files.
Currently Sheetz only handles .xlsx, .xls, and .csv.

**Requested API (no change to existing API):**
```java
// Should work identically to .xlsx
List<Product> products = Sheetz.read("data.ods", Product.class);
Sheetz.write(products, "output.ods");
```

**Implementation notes:**
- Apache POI already has ODS support via `org.apache.poi:poi-ooxml`
- Follow the pattern of `XlsxReader.java` / `XlsxWriter.java`
- Add `OdsReader.java` and `OdsWriter.java` implementing the same interfaces
- Register the `.ods` extension in `FormatDetector.java`
- Add test resources: a sample `.ods` file

**Difficulty:** Medium (~300 lines of new code)
**Great first major feature contribution!**

---

### Issue 2
**Title:** Spring Boot Auto-Configuration Starter
**Labels:** enhancement, help wanted
**Body:**
## Feature Request: Spring Boot Starter

A Spring Boot starter would allow zero-config injection of Sheetz into Spring applications:

```java
@Service
public class ReportService {
    @Autowired
    private SheetzTemplate sheetz; // Auto-configured bean

    public List<Product> importProducts(MultipartFile file) {
        return sheetz.read(file.getInputStream(), Product.class);
    }
}
```

**New repo:** `chitralabs/sheetz-spring-boot-starter`
**Maven artifact:** `io.github.chitralabs.sheetz:sheetz-spring-boot-starter`

This would be published as a separate Maven artifact to Maven Central,
dramatically expanding Sheetz's reach into the Spring Boot ecosystem.

---

### Issue 3
**Title:** Add benchmark chart images to sheetz-benchmarks README
**Labels:** documentation, good first issue
**Body:**
## Improvement: Visual Benchmark Charts

The sheetz-benchmarks README has great benchmark tables but no visual charts.
A bar chart image showing Sheetz vs Apache POI would be much more shareable on Reddit/Twitter.

**Task:**
1. Use JMH Visualizer (https://jmh.morethan.io) with `results/results.txt`
2. Export chart images as PNG
3. Add to `results/` directory
4. Reference in README with `![Chart](results/write-chart.png)`

**Difficulty:** Very Easy — no Java coding required
**Time estimate:** ~30 minutes

---

### Issue 4
**Title:** Add multi-JDK CI matrix to sheetz-examples
**Labels:** ci, good first issue
**Body:**
## Improvement: Test Examples on Java 17 and 21

The sheetz-examples CI currently only tests on one Java version.
We should verify examples run correctly on Java 11, 17, and 21.

**Task:**
Update `.github/workflows/ci.yml` in sheetz-examples to add:
```yaml
matrix:
  java: ['11', '17', '21']
  os: [ubuntu-latest, windows-latest]
```

**Difficulty:** Very Easy — just YAML changes
**Time estimate:** ~15 minutes

---

### Issue 5
**Title:** Publish sheetz-examples artifact to Maven Central
**Labels:** enhancement, help wanted
**Body:**
## Feature: Publish Examples to Maven Central

Publishing the `sheetz-examples` project to Maven Central as an artifact would:
1. Add 1 more "used in" dependent for sheetz-core on Maven Central
2. Make examples easily runnable via Maven without cloning

**Task:**
- Update `sheetz-examples/pom.xml` with publishing configuration
- Add GPG signing profile (follow `sheetz-core` pom.xml as template)
- Publish to Maven Central as `io.github.chitralabs.sheetz:sheetz-examples:1.0.1`
