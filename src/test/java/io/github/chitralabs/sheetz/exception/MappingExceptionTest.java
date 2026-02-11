package io.github.chitralabs.sheetz.exception;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MappingException message formatting.
 */
class MappingExceptionTest {

    @Test void testRowOnlyMessage() {
        MappingException e = new MappingException("Field is empty", 5);
        assertThat(e.getMessage()).isEqualTo("Row 5: Field is empty");
        assertThat(e.row()).isEqualTo(5);
        assertThat(e.column()).isNull();
        assertThat(e.value()).isNull();
    }

    @Test void testFullMessage() {
        MappingException e = new MappingException("Cannot convert", 3, "price", "abc");
        assertThat(e.getMessage()).contains("Row 3");
        assertThat(e.getMessage()).contains("column 'price'");
        assertThat(e.getMessage()).contains("Cannot convert");
        assertThat(e.getMessage()).contains("value: abc");
        assertThat(e.row()).isEqualTo(3);
        assertThat(e.column()).isEqualTo("price");
        assertThat(e.value()).isEqualTo("abc");
    }

    @Test void testWithCause() {
        RuntimeException cause = new RuntimeException("original");
        MappingException e = new MappingException("wrapper", 1, "col", "val", cause);
        assertThat(e.getCause()).isEqualTo(cause);
    }

    @Test void testNullColumn() {
        MappingException e = new MappingException("error", 1, null, "val");
        assertThat(e.getMessage()).doesNotContain("column");
    }

    @Test void testNullValue() {
        MappingException e = new MappingException("error", 1, "col", null);
        assertThat(e.getMessage()).doesNotContain("value:");
    }

    @Test void testIsSheetzException() {
        MappingException e = new MappingException("test", 1);
        assertThat(e).isInstanceOf(SheetzException.class);
        assertThat(e).isInstanceOf(RuntimeException.class);
    }
}
