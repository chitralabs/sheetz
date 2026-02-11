package io.github.chitralabs.sheetz;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ValidationResult.
 */
class ValidationResultTest {

    @Test void testValidResult() {
        ValidationResult<String> result = new ValidationResult<>(
            List.of("a", "b"), Collections.emptyList(), 2, 10);
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.validCount()).isEqualTo(2);
        assertThat(result.errorCount()).isEqualTo(0);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.successRate()).isEqualTo(100.0);
        assertThat(result.durationMs()).isEqualTo(10);
    }

    @Test void testResultWithErrors() {
        List<ValidationResult.RowError> errors = List.of(
            new ValidationResult.RowError(2, "col", "error msg", "bad", null),
            new ValidationResult.RowError(4, "another error")
        );
        ValidationResult<String> result = new ValidationResult<>(List.of("ok"), errors, 3, 5);
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.validCount()).isEqualTo(1);
        assertThat(result.errorCount()).isEqualTo(2);
        assertThat(result.totalRows()).isEqualTo(3);
    }

    @Test void testSuccessRate() {
        ValidationResult<String> half = new ValidationResult<>(List.of("a"), List.of(new ValidationResult.RowError(1, "e")), 2, 0);
        assertThat(half.successRate()).isEqualTo(50.0);

        ValidationResult<String> empty = new ValidationResult<>(Collections.emptyList(), Collections.emptyList(), 0, 0);
        assertThat(empty.successRate()).isEqualTo(100.0); // Edge case: no rows = 100%
    }

    @Test void testRowError() {
        ValidationResult.RowError error = new ValidationResult.RowError(5, "colA", "value missing", "null", new RuntimeException("cause"));
        assertThat(error.row()).isEqualTo(5);
        assertThat(error.column()).isEqualTo("colA");
        assertThat(error.message()).isEqualTo("value missing");
        assertThat(error.value()).isEqualTo("null");
        assertThat(error.cause()).isInstanceOf(RuntimeException.class);
    }

    @Test void testRowErrorSimple() {
        ValidationResult.RowError error = new ValidationResult.RowError(3, "simple message");
        assertThat(error.row()).isEqualTo(3);
        assertThat(error.column()).isNull();
        assertThat(error.value()).isNull();
        assertThat(error.cause()).isNull();
    }

    @Test void testImmutability() {
        List<String> rows = new ArrayList<>(List.of("a", "b"));
        ValidationResult<String> result = new ValidationResult<>(rows, Collections.emptyList(), 2, 0);
        rows.add("c"); // Modify original list
        assertThat(result.validRows()).hasSize(2); // Should not be affected
        assertThatThrownBy(() -> result.validRows().add("d")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test void testNullLists() {
        ValidationResult<String> result = new ValidationResult<>(null, null, 0, 0);
        assertThat(result.validRows()).isEmpty();
        assertThat(result.errors()).isEmpty();
    }
}
