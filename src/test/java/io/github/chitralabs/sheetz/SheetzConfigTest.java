package io.github.chitralabs.sheetz;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for SheetzConfig.
 */
class SheetzConfigTest {

    @Test void testDefaults() {
        SheetzConfig config = SheetzConfig.defaults();
        assertThat(config.dateFormat()).isEqualTo("yyyy-MM-dd");
        assertThat(config.dateTimeFormat()).isEqualTo("yyyy-MM-dd HH:mm:ss");
        assertThat(config.timeFormat()).isEqualTo("HH:mm:ss");
        assertThat(config.trimValues()).isTrue();
        assertThat(config.skipEmptyRows()).isTrue();
        assertThat(config.defaultSheetName()).isEqualTo("Sheet1");
        assertThat(config.headerRow()).isEqualTo(0);
        assertThat(config.batchSize()).isEqualTo(1000);
        assertThat(config.evaluateFormulas()).isTrue();
        assertThat(config.streamingThreshold()).isEqualTo(10000);
    }

    @Test void testBuilder() {
        SheetzConfig config = SheetzConfig.builder()
            .dateFormat("dd/MM/yyyy")
            .dateTimeFormat("dd/MM/yyyy HH:mm")
            .timeFormat("HH:mm")
            .trimValues(false)
            .skipEmptyRows(false)
            .defaultSheetName("Data")
            .headerRow(1)
            .batchSize(500)
            .evaluateFormulas(false)
            .streamingThreshold(5000)
            .build();

        assertThat(config.dateFormat()).isEqualTo("dd/MM/yyyy");
        assertThat(config.trimValues()).isFalse();
        assertThat(config.skipEmptyRows()).isFalse();
        assertThat(config.headerRow()).isEqualTo(1);
        assertThat(config.batchSize()).isEqualTo(500);
        assertThat(config.streamingThreshold()).isEqualTo(5000);
    }

    @Test void testBuilderValidation() {
        assertThatThrownBy(() -> SheetzConfig.builder().headerRow(-1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SheetzConfig.builder().batchSize(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SheetzConfig.builder().streamingThreshold(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SheetzConfig.builder().dateFormat(null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SheetzConfig.builder().defaultSheetName(null))
            .isInstanceOf(NullPointerException.class);
    }
}
