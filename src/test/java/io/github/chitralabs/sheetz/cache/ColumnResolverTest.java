package io.github.chitralabs.sheetz.cache;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ColumnResolver fuzzy matching logic.
 */
class ColumnResolverTest {

    @Test void testExactMatch() {
        ColumnResolver r = new ColumnResolver(List.of("Name", "Price", "Status"));
        assertThat(r.resolve("Name")).isEqualTo(0);
        assertThat(r.resolve("Price")).isEqualTo(1);
        assertThat(r.resolve("Status")).isEqualTo(2);
    }

    @Test void testCaseInsensitiveMatch() {
        ColumnResolver r = new ColumnResolver(List.of("Name", "Price"));
        assertThat(r.resolve("name")).isEqualTo(0);
        assertThat(r.resolve("NAME")).isEqualTo(0);
        assertThat(r.resolve("pRiCe")).isEqualTo(1);
    }

    @Test void testNormalizedMatch() {
        ColumnResolver r = new ColumnResolver(List.of("Product Name", "Unit Price", "In-Stock"));
        // Underscores and spaces should normalize away
        assertThat(r.resolve("product_name")).isEqualTo(0);
        assertThat(r.resolve("ProductName")).isEqualTo(0);
        assertThat(r.resolve("unitprice")).isEqualTo(1);
        assertThat(r.resolve("in_stock")).isEqualTo(2);
        assertThat(r.resolve("instock")).isEqualTo(2);
    }

    @Test void testReturnsMinusOneForUnknown() {
        ColumnResolver r = new ColumnResolver(List.of("Name", "Price"));
        assertThat(r.resolve("Unknown")).isEqualTo(-1);
        assertThat(r.resolve("")).isEqualTo(-1);
    }

    @Test void testNullInput() {
        ColumnResolver r = new ColumnResolver(List.of("Name"));
        assertThat(r.resolve(null)).isEqualTo(-1);
    }

    @Test void testHeaderAt() {
        ColumnResolver r = new ColumnResolver(List.of("A", "B", "C"));
        assertThat(r.headerAt(0)).isEqualTo("A");
        assertThat(r.headerAt(2)).isEqualTo("C");
        assertThat(r.headerAt(-1)).isNull();
        assertThat(r.headerAt(10)).isNull();
    }

    @Test void testSize() {
        assertThat(new ColumnResolver(List.of("A", "B")).size()).isEqualTo(2);
        assertThat(new ColumnResolver(Collections.emptyList()).size()).isEqualTo(0);
    }

    @Test void testDuplicateHeaders() {
        ColumnResolver r = new ColumnResolver(List.of("Name", "Name", "Price"));
        // First occurrence wins
        assertThat(r.resolve("Name")).isEqualTo(0);
    }

    @Test void testNullHeaders() {
        ColumnResolver r = new ColumnResolver(Arrays.asList("Name", null, "Price"));
        assertThat(r.resolve("Name")).isEqualTo(0);
        assertThat(r.resolve("Price")).isEqualTo(2);
        assertThat(r.size()).isEqualTo(3);
    }

    @Test void testUnicodeHeaders() {
        ColumnResolver r = new ColumnResolver(List.of("名前", "Ünïcödé", "日本語ヘッダー"));
        assertThat(r.resolve("名前")).isEqualTo(0);
        assertThat(r.resolve("ünïcödé")).isEqualTo(1);
    }

    @Test void testWhitespaceNormalization() {
        ColumnResolver r = new ColumnResolver(List.of("First Name", "Last  Name"));
        assertThat(r.resolve("first-name")).isEqualTo(0);
        assertThat(r.resolve("FIRST_NAME")).isEqualTo(0);
    }

    @Test void testEmptyHeaders() {
        ColumnResolver r = new ColumnResolver(List.of("", "A", ""));
        assertThat(r.resolve("A")).isEqualTo(1);
    }

    @Test void testExactMatchTakesPrecedenceOverFuzzy() {
        // If "name" exists exactly at index 1, and normalized "Name" might match index 0,
        // exact match should win
        ColumnResolver r = new ColumnResolver(List.of("Name", "name"));
        assertThat(r.resolve("Name")).isEqualTo(0);
        assertThat(r.resolve("name")).isEqualTo(1);
    }
}
