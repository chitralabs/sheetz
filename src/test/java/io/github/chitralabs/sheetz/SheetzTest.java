package io.github.chitralabs.sheetz;

import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;
import io.github.chitralabs.sheetz.reader.StreamingReader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for Sheetz library.
 */
class SheetzTest {
    @TempDir Path tempDir;
    @BeforeEach void setUp() { Sheetz.reset(); }

    // ==================== Basic Read/Write Tests ====================

    @Test void testExcelRoundtrip() {
        Path file = tempDir.resolve("test.xlsx");
        List<Product> products = List.of(new Product("Widget", 19.99, true, LocalDate.of(2024, 1, 15)), new Product("Gadget", 49.99, false, LocalDate.of(2024, 2, 20)));
        Sheetz.write(products, file);
        assertThat(Files.exists(file)).isTrue();
        List<Product> result = Sheetz.read(file, Product.class);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name).isEqualTo("Widget");
        assertThat(result.get(0).price).isEqualTo(19.99);
    }

    @Test void testCsvRoundtrip() throws IOException {
        Path file = tempDir.resolve("test.csv");
        List<Product> products = List.of(new Product("Widget", 19.99, true, LocalDate.of(2024, 1, 15)));
        Sheetz.write(products, file);
        String content = Files.readString(file);
        assertThat(content).contains("Widget", "19.99");
        List<Product> result = Sheetz.read(file, Product.class);
        assertThat(result).hasSize(1);
    }

    @Test void testXlsFormat() {
        Path file = tempDir.resolve("test.xls");
        List<SimpleProduct> products = List.of(new SimpleProduct("Widget", 19.99));
        Sheetz.write(products, file);
        assertThat(Files.exists(file)).isTrue();
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name).isEqualTo("Widget");
    }

    // ==================== Annotation Tests ====================

    @Test void testAnnotatedColumns() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "Product Name,Unit Price,In Stock\nWidget,19.99,yes\nGadget,49.99,no");
        List<AnnotatedProduct> result = Sheetz.read(file, AnnotatedProduct.class);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name).isEqualTo("Widget");
        assertThat(result.get(0).inStock).isTrue();
    }

    @Test void testRequiredField() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,\nGadget,49.99");
        ValidationResult<RequiredProduct> result = Sheetz.validate(file, RequiredProduct.class);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.validCount()).isEqualTo(1);
    }

    @Test void testDefaultValue() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,status\nWidget,\nGadget,active");
        List<DefaultValueProduct> result = Sheetz.read(file, DefaultValueProduct.class);
        assertThat(result.get(0).status).isEqualTo("pending");
        assertThat(result.get(1).status).isEqualTo("active");
    }

    @Test void testColumnIndex() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "a,b,c\nfirst,second,third");
        List<IndexedProduct> result = Sheetz.read(file, IndexedProduct.class);
        assertThat(result.get(0).first).isEqualTo("first");
        assertThat(result.get(0).third).isEqualTo("third");
    }

    // ==================== Type Conversion Tests ====================

    @Test void testNumericTypes() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "intVal,longVal,doubleVal,floatVal,bigDecimal\n42,123456789012,3.14,2.5,99.99");
        List<NumericTypes> result = Sheetz.read(file, NumericTypes.class);
        assertThat(result.get(0).intVal).isEqualTo(42);
        assertThat(result.get(0).longVal).isEqualTo(123456789012L);
        assertThat(result.get(0).doubleVal).isEqualTo(3.14);
    }

    @Test void testBooleanVariations() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "val\ntrue\nfalse\nyes\nno\n1\n0\non\noff\nY\nN");
        List<BooleanHolder> result = Sheetz.read(file, BooleanHolder.class);
        assertThat(result).extracting("val").containsExactly(true, false, true, false, true, false, true, false, true, false);
    }

    @Test void testEnumConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "status\nACTIVE\ninactive\nPending");
        List<StatusHolder> result = Sheetz.read(file, StatusHolder.class);
        assertThat(result).extracting("status").containsExactly(Status.ACTIVE, Status.INACTIVE, Status.PENDING);
    }

    @Test void testShortAndByteConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "shortVal,byteVal\n32767,127\n-32768,-128\n100,50");
        List<ShortByteHolder> result = Sheetz.read(file, ShortByteHolder.class);
        assertThat(result.get(0).shortVal).isEqualTo((short) 32767);
        assertThat(result.get(0).byteVal).isEqualTo((byte) 127);
    }

    @Test void testCharacterConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "charVal\nA\nB\nHello");
        List<CharHolder> result = Sheetz.read(file, CharHolder.class);
        assertThat(result.get(0).charVal).isEqualTo('A');
        assertThat(result.get(2).charVal).isEqualTo('H');
    }

    @Test void testInstantConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "instant\n2024-01-15T10:30:00Z\n2024-06-20T15:45:30Z");
        List<InstantHolder> result = Sheetz.read(file, InstantHolder.class);
        assertThat(result.get(0).instant).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
    }

    @Test void testZonedDateTimeConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "zdt\n2024-01-15T10:30:00+01:00[Europe/Paris]\n2024-06-20T15:45:30-05:00[America/New_York]");
        List<ZonedDateTimeHolder> result = Sheetz.read(file, ZonedDateTimeHolder.class);
        assertThat(result.get(0).zdt.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test void testUUIDConversion() throws IOException {
        Path file = tempDir.resolve("test.csv");
        UUID uuid1 = UUID.randomUUID();
        Files.writeString(file, "uuid\n" + uuid1);
        List<UUIDHolder> result = Sheetz.read(file, UUIDHolder.class);
        assertThat(result.get(0).uuid).isEqualTo(uuid1);
    }

    @Test void testDateTypes() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "localDate,localDateTime,localTime\n2024-01-15,2024-01-15T10:30:00,10:30:00");
        List<DateTypes> result = Sheetz.read(file, DateTypes.class);
        assertThat(result.get(0).localDate).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(result.get(0).localDateTime).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        assertThat(result.get(0).localTime).isEqualTo(LocalTime.of(10, 30, 0));
    }

    // ==================== Untyped Reading Tests ====================

    @Test void testReadMaps() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,19.99");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("name", "Widget");
    }

    @Test void testReadMapsFromInputStream() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,19.99");
        try (InputStream is = Files.newInputStream(file)) {
            List<Map<String, Object>> result = Sheetz.readMaps(is, Format.CSV);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).containsEntry("name", "Widget");
        }
    }

    @Test void testReadRaw() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "a,b,c\n1,2,3");
        List<String[]> result = Sheetz.readRaw(file);
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly("a", "b", "c");
    }

    @Test void testReadRawFromInputStream() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "a,b,c\n1,2,3");
        try (InputStream is = Files.newInputStream(file)) {
            List<String[]> result = Sheetz.readRaw(is, Format.CSV);
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsExactly("a", "b", "c");
        }
    }

    // ==================== readFirst Tests ====================

    @Test void testReadFirst() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 100; i++) products.add(new SimpleProduct("P" + i, i * 1.0));
        Sheetz.write(products, file);

        List<SimpleProduct> first5 = Sheetz.readFirst(file, SimpleProduct.class, 5);
        assertThat(first5).hasSize(5);
        assertThat(first5.get(0).name).isEqualTo("P0");
        assertThat(first5.get(4).name).isEqualTo("P4");
    }

    @Test void testReadFirstZero() {
        Path file = tempDir.resolve("test.xlsx");
        Sheetz.write(List.of(new SimpleProduct("A", 1.0)), file);
        assertThat(Sheetz.readFirst(file, SimpleProduct.class, 0)).isEmpty();
    }

    @Test void testReadFirstMoreThanAvailable() {
        Path file = tempDir.resolve("test.xlsx");
        Sheetz.write(List.of(new SimpleProduct("A", 1.0), new SimpleProduct("B", 2.0)), file);
        List<SimpleProduct> result = Sheetz.readFirst(file, SimpleProduct.class, 100);
        assertThat(result).hasSize(2);
    }

    // ==================== Multi-Sheet Tests ====================

    @Test void testMultiSheet() {
        Path file = tempDir.resolve("multi.xlsx");
        List<SimpleProduct> products = List.of(new SimpleProduct("Widget", 19.99));
        List<Employee> employees = List.of(new Employee("John", "Engineering", 75000));
        Sheetz.workbook().sheet("Products", products).sheet("Employees", employees).write(file);
        assertThat(Files.exists(file)).isTrue();
    }

    @Test void testReadSpecificSheet() {
        Path file = tempDir.resolve("multi.xlsx");
        List<SimpleProduct> products = List.of(new SimpleProduct("Widget", 19.99));
        List<SimpleProduct> otherProducts = List.of(new SimpleProduct("Gadget", 29.99), new SimpleProduct("Gizmo", 39.99));
        Sheetz.workbook().sheet("Products", products).sheet("OtherProducts", otherProducts).write(file);

        List<SimpleProduct> result = Sheetz.reader(SimpleProduct.class).file(file).sheet("OtherProducts").read();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name).isEqualTo("Gadget");

        List<SimpleProduct> result2 = Sheetz.reader(SimpleProduct.class).file(file).sheet(1).read();
        assertThat(result2).hasSize(2);
    }

    // ==================== Streaming Tests ====================

    @Test void testStreamingForEach() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 100; i++) products.add(new SimpleProduct("Product" + i, i * 10.0));
        Sheetz.write(products, file);
        AtomicInteger count = new AtomicInteger(0);
        Sheetz.stream(file, SimpleProduct.class).forEach(p -> count.incrementAndGet());
        assertThat(count.get()).isEqualTo(100);
    }

    @Test void testStreamingBatch() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 100; i++) products.add(new SimpleProduct("Product" + i, i * 10.0));
        Sheetz.write(products, file);
        List<Integer> batchSizes = new ArrayList<>();
        Sheetz.stream(file, SimpleProduct.class).batch(30).forEach(batch -> batchSizes.add(batch.size()));
        assertThat(batchSizes).containsExactly(30, 30, 30, 10);
    }

    @Test void testStreamingStream() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 50; i++) products.add(new SimpleProduct("Product" + i, i * 10.0));
        Sheetz.write(products, file);
        long count = Sheetz.stream(file, SimpleProduct.class).stream().filter(p -> p.price > 200).count();
        assertThat(count).isEqualTo(29);
    }

    @Test void testStreamingCsv() {
        Path file = tempDir.resolve("test.csv");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 50; i++) products.add(new SimpleProduct("Product" + i, i * 10.0));
        Sheetz.write(products, file);
        AtomicInteger count = new AtomicInteger(0);
        Sheetz.stream(file, SimpleProduct.class).forEach(p -> count.incrementAndGet());
        assertThat(count.get()).isEqualTo(50);
    }

    @Test void testStreamingAutoCloseable() {
        Path file = tempDir.resolve("test.xlsx");
        Sheetz.write(List.of(new SimpleProduct("Widget", 19.99)), file);
        try (StreamingReader<SimpleProduct> reader = Sheetz.stream(file, SimpleProduct.class)) {
            AtomicInteger count = new AtomicInteger(0);
            reader.forEach(p -> count.incrementAndGet());
            assertThat(count.get()).isEqualTo(1);
        }
    }

    @Test void testStreamingCloseInterruptsParser() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 1000; i++) products.add(new SimpleProduct("P" + i, i * 1.0));
        Sheetz.write(products, file);

        StreamingReader<SimpleProduct> reader = Sheetz.stream(file, SimpleProduct.class);
        Iterator<SimpleProduct> it = reader.iterator();
        // Read only a few items then close
        if (it.hasNext()) it.next();
        reader.close();
        // After close, creating a new iterator should throw
        assertThatThrownBy(reader::iterator).isInstanceOf(SheetzException.class).hasMessageContaining("closed");
    }

    // ==================== Validation Tests ====================

    @Test void testValidation() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,19.99\n,invalid\nGadget,49.99");
        ValidationResult<RequiredProduct> result = Sheetz.validate(file, RequiredProduct.class);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.validCount()).isEqualTo(2);
    }

    @Test void testValidationMetrics() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,19.99\n,invalid\nGadget,49.99\n,bad");
        ValidationResult<RequiredProduct> result = Sheetz.validate(file, RequiredProduct.class);
        assertThat(result.totalRows()).isEqualTo(4);
        assertThat(result.validCount()).isEqualTo(2);
        assertThat(result.errorCount()).isEqualTo(2);
        // successRate() returns percentage (0-100), not ratio (0-1)
        assertThat(result.successRate()).isEqualTo(50.0);
    }

    @Test void testValidationNoErrors() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price\nWidget,19.99\nGadget,49.99");
        ValidationResult<RequiredProduct> result = Sheetz.validate(file, RequiredProduct.class);
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.validCount()).isEqualTo(2);
        assertThat(result.durationMs()).isGreaterThanOrEqualTo(0);
    }

    // ==================== Configuration Tests ====================

    @Test void testCustomConfig() {
        SheetzConfig cfg = SheetzConfig.builder().dateFormat("dd/MM/yyyy").batchSize(500).build();
        Sheetz.configure(cfg);
        assertThat(Sheetz.config().dateFormat()).isEqualTo("dd/MM/yyyy");
        assertThat(Sheetz.config().batchSize()).isEqualTo(500);
    }

    @Test void testCustomDateFormat() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "date\n15/01/2024\n20/06/2024");
        Sheetz.configure(SheetzConfig.builder().dateFormat("dd/MM/yyyy").build());
        List<DateHolder> result = Sheetz.read(file, DateHolder.class);
        assertThat(result.get(0).date).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    // ==================== Error Handling Tests ====================

    @Test void testFileNotFound() {
        assertThatThrownBy(() -> Sheetz.read(tempDir.resolve("nonexistent.xlsx"), Product.class)).isInstanceOf(SheetzException.class).hasMessageContaining("File not found");
    }

    @Test void testUnsupportedFormat() {
        assertThatThrownBy(() -> Format.detect("file.txt")).isInstanceOf(SheetzException.class).hasMessageContaining("Unsupported format");
    }

    @Test void testEmptyDataWrite() {
        Path file = tempDir.resolve("test.xlsx");
        assertThatThrownBy(() -> Sheetz.write(Collections.emptyList(), file)).isInstanceOf(SheetzException.class).hasMessageContaining("empty");
    }

    @Test void testNullDataWrite() {
        Path file = tempDir.resolve("test.xlsx");
        assertThatThrownBy(() -> Sheetz.write(null, file)).isInstanceOf(SheetzException.class);
    }

    @Test void testNullPathRead() {
        assertThatThrownBy(() -> Sheetz.read((Path) null, Product.class)).isInstanceOf(NullPointerException.class);
    }

    // ==================== Edge Case Tests ====================

    @Test void testUnicodeHeaders() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "名前,価格\n太郎,100\n花子,200");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("名前", "太郎");
        assertThat(result.get(0)).containsEntry("価格", "100");
    }

    @Test void testUnicodeValues() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,description\nWidget,Ünïcödé spëcïal chàracters\nGadget,中文描述");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result.get(0).get("description")).isEqualTo("Ünïcödé spëcïal chàracters");
        assertThat(result.get(1).get("description")).isEqualTo("中文描述");
    }

    @Test void testCsvWithEmbeddedNewlines() throws IOException {
        Path file = tempDir.resolve("test.csv");
        // CSV with quoted field containing a newline
        Files.writeString(file, "name,description\n\"Widget\",\"A product\nwith a newline\"\nGadget,Simple");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("description")).isEqualTo("A product\nwith a newline");
    }

    @Test void testCsvWithEmbeddedCommas() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,description\n\"Widget, Inc.\",\"The best\"\nGadget,OK");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("name")).isEqualTo("Widget, Inc.");
    }

    @Test void testCsvWithEscapedQuotes() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,description\nWidget,\"She said \"\"hello\"\"\"\nGadget,OK");
        List<Map<String, Object>> result = Sheetz.readMaps(file);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("description")).isEqualTo("She said \"hello\"");
    }

    @Test void testEmptyCsvFile() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "");
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).isEmpty();
    }

    @Test void testHeaderOnlyCsvFile() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price");
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).isEmpty();
    }

    @Test void testHeaderOnlyExcelFile() {
        Path file = tempDir.resolve("test.xlsx");
        // Write then immediately read back with no data rows
        List<SimpleProduct> products = List.of(new SimpleProduct("X", 1.0));
        Sheetz.write(products, file);
        // Reading should work
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).hasSize(1);
    }

    @Test void testInheritedFields() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "name,price,category\nWidget,19.99,Tools");
        List<CategorizedProduct> result = Sheetz.read(file, CategorizedProduct.class);
        assertThat(result.get(0).name).isEqualTo("Widget");
        assertThat(result.get(0).price).isEqualTo(19.99);
        assertThat(result.get(0).category).isEqualTo("Tools");
    }

    @Test void testBadConverterThrowsSheetzException() {
        assertThatThrownBy(() -> {
            Path file = tempDir.resolve("test.csv");
            Files.writeString(file, "val\nhello");
            Sheetz.read(file, BadConverterHolder.class);
        }).isInstanceOf(SheetzException.class)
          .hasMessageContaining("Failed to instantiate converter");
    }

    // ==================== Thread Safety Tests ====================

    @Test void testConcurrentReads() throws Exception {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = List.of(new SimpleProduct("A", 10.0), new SimpleProduct("B", 20.0));
        Sheetz.write(products, file);
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> { try { List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class); if (result.size() == 2) successCount.incrementAndGet(); } finally { latch.countDown(); } });
        }
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(threads);
    }

    @Test void testConcurrentWrites() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    Path file = tempDir.resolve("concurrent-" + idx + ".xlsx");
                    Sheetz.write(List.of(new SimpleProduct("Product" + idx, idx * 10.0)), file);
                    if (Files.exists(file)) successCount.incrementAndGet();
                } finally { latch.countDown(); }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(threads);
    }

    // ==================== Builder API Tests ====================

    @Test void testReaderBuilder() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "a;b;c\n1;2;3");
        List<SimpleRecord> result = Sheetz.reader(SimpleRecord.class).file(file).delimiter(';').read();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).a).isEqualTo("1");
    }

    @Test void testWriterBuilder() {
        Path file = tempDir.resolve("test.xlsx");
        Sheetz.writer(SimpleProduct.class).data(List.of(new SimpleProduct("Widget", 19.99))).file(file).sheet("MySheet").autoSize(true).freezeHeader(true).write();
        assertThat(Files.exists(file)).isTrue();
    }

    @Test void testWriterBuilderStreaming() {
        Path file = tempDir.resolve("test.xlsx");
        List<SimpleProduct> products = new ArrayList<>();
        for (int i = 0; i < 100; i++) products.add(new SimpleProduct("Product" + i, i * 10.0));
        Sheetz.writer(SimpleProduct.class).data(products).file(file).streaming(true).write();
        assertThat(Files.exists(file)).isTrue();
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).hasSize(100);
    }

    // ==================== Custom Converter Tests ====================

    @Test void testCustomConverter() throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "amount\n$100.50\n$200.00");
        List<MoneyHolder> result = Sheetz.read(file, MoneyHolder.class);
        assertThat(result.get(0).amount).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test void testGlobalConverter() throws IOException {
        Sheetz.register(Money.class, new MoneyConverter2());
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "money\n$50.00\n$75.50");
        List<MoneyHolder2> result = Sheetz.read(file, MoneyHolder2.class);
        assertThat(result.get(0).money.amount).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ==================== InputStream/OutputStream Tests ====================

    @Test void testReadFromInputStream() throws IOException {
        Path file = tempDir.resolve("test.xlsx");
        Sheetz.write(List.of(new SimpleProduct("Widget", 19.99)), file);
        try (InputStream is = Files.newInputStream(file)) {
            List<SimpleProduct> result = Sheetz.read(is, SimpleProduct.class, Format.XLSX);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name).isEqualTo("Widget");
        }
    }

    @Test void testWriteToOutputStream() throws IOException {
        Path file = tempDir.resolve("test.xlsx");
        try (OutputStream os = Files.newOutputStream(file)) {
            Sheetz.write(List.of(new SimpleProduct("Widget", 19.99)), os, Format.XLSX);
        }
        List<SimpleProduct> result = Sheetz.read(file, SimpleProduct.class);
        assertThat(result).hasSize(1);
    }

    // ==================== Test Model Classes ====================

    public static class Product {
        public String name; public Double price; public Boolean available; public LocalDate releaseDate;
        public Product() {} public Product(String n, Double p, Boolean a, LocalDate r) { name=n; price=p; available=a; releaseDate=r; }
    }
    public static class SimpleProduct {
        public String name; public Double price;
        public SimpleProduct() {} public SimpleProduct(String n, Double p) { name=n; price=p; }
    }
    public static class AnnotatedProduct {
        @Column("Product Name") public String name;
        @Column("Unit Price") public Double price;
        @Column("In Stock") public Boolean inStock;
    }
    public static class RequiredProduct { public String name; @Column(required = true) public Double price; }
    public static class DefaultValueProduct { public String name; @Column(defaultValue = "pending") public String status; }
    public static class IndexedProduct { @Column(index = 0) public String first; @Column(index = 2) public String third; }
    public static class NumericTypes { public Integer intVal; public Long longVal; public Double doubleVal; public Float floatVal; public BigDecimal bigDecimal; }
    public static class BooleanHolder { public Boolean val; }
    public static class ShortByteHolder { public Short shortVal; public Byte byteVal; }
    public static class CharHolder { public Character charVal; }
    public static class InstantHolder { public Instant instant; }
    public static class ZonedDateTimeHolder { public ZonedDateTime zdt; }
    public static class UUIDHolder { public UUID uuid; }
    public static class DateTypes { public LocalDate localDate; public LocalDateTime localDateTime; public LocalTime localTime; }
    public static class DateHolder { public LocalDate date; }
    public enum Status { ACTIVE, INACTIVE, PENDING }
    public static class StatusHolder { public Status status; }
    public static class Employee { public String name; public String department; public Integer salary; public Employee() {} public Employee(String n, String d, Integer s) { name=n; department=d; salary=s; } }
    public static class SimpleRecord { public String a; public String b; public String c; }
    public static class MoneyHolder { @Column(converter = DollarConverter.class) public BigDecimal amount; }
    public static class DollarConverter implements Converter<BigDecimal> {
        public BigDecimal fromCell(Object v, ConvertContext ctx) { if (v == null) return null; return new BigDecimal(v.toString().replace("$", "").trim()); }
        public Object toCell(BigDecimal v) { return "$" + v; }
    }
    public static class Money { public BigDecimal amount; public Money(BigDecimal a) { amount = a; } }
    public static class MoneyHolder2 { public Money money; }
    public static class MoneyConverter2 implements Converter<Money> {
        public Money fromCell(Object v, ConvertContext ctx) { if (v == null) return null; return new Money(new BigDecimal(v.toString().replace("$", "").trim())); }
        public Object toCell(Money v) { return v != null ? "$" + v.amount : null; }
    }
    // Inheritance test class
    public static class CategorizedProduct extends SimpleProduct {
        public String category;
        public CategorizedProduct() {}
    }
    // Bad converter (no public no-arg constructor)
    public static class BadConverterHolder { @Column(converter = PrivateConverter.class) public String val; }
    private static class PrivateConverter implements Converter<String> {
        private PrivateConverter(int dummy) {}
        public String fromCell(Object v, ConvertContext ctx) { return null; }
        public Object toCell(String v) { return null; }
    }
}
