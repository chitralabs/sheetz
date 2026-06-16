package io.github.chitralabs.sheetz;

import io.github.chitralabs.sheetz.exception.SheetzException;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Format enum.
 */
class FormatTest {

    @Test void testDetectXlsx() {
        assertThat(Format.detect("file.xlsx")).isEqualTo(Format.XLSX);
        assertThat(Format.detect("FILE.XLSX")).isEqualTo(Format.XLSX);
        assertThat(Format.detect("/path/to/file.xlsx")).isEqualTo(Format.XLSX);
    }

    @Test void testDetectXls() {
        assertThat(Format.detect("file.xls")).isEqualTo(Format.XLS);
        assertThat(Format.detect("FILE.XLS")).isEqualTo(Format.XLS);
    }

    @Test void testDetectCsv() {
        assertThat(Format.detect("file.csv")).isEqualTo(Format.CSV);
        assertThat(Format.detect("FILE.CSV")).isEqualTo(Format.CSV);
    }

    @Test void testDetectTsv() {
        assertThat(Format.detect("file.tsv")).isEqualTo(Format.CSV);
    }

    @Test void testDetectWithSpaces() {
        assertThat(Format.detect("  file.xlsx  ")).isEqualTo(Format.XLSX);
    }

    @Test void testDetectUnsupported() {
        assertThatThrownBy(() -> Format.detect("file.txt")).isInstanceOf(SheetzException.class);
        assertThatThrownBy(() -> Format.detect("file.pdf")).isInstanceOf(SheetzException.class);
        assertThatThrownBy(() -> Format.detect("file")).isInstanceOf(SheetzException.class);
    }

    @Test void testDetectNull() {
        assertThatThrownBy(() -> Format.detect(null)).isInstanceOf(NullPointerException.class);
    }

    @Test void testDetectOds() {
        assertThat(Format.detect("file.ods")).isEqualTo(Format.ODS);
        assertThat(Format.detect("FILE.ODS")).isEqualTo(Format.ODS);
        assertThat(Format.detect("/path/to/file.ods")).isEqualTo(Format.ODS);
    }

    @Test void testProperties() {
        assertThat(Format.XLSX.extension()).isEqualTo("xlsx");
        assertThat(Format.XLSX.isExcel()).isTrue();
        assertThat(Format.XLSX.isCsv()).isFalse();
        assertThat(Format.XLSX.isOds()).isFalse();
        assertThat(Format.XLSX.isSpreadsheet()).isTrue();

        assertThat(Format.XLS.extension()).isEqualTo("xls");
        assertThat(Format.XLS.isExcel()).isTrue();
        assertThat(Format.XLS.isCsv()).isFalse();
        assertThat(Format.XLS.isSpreadsheet()).isTrue();

        assertThat(Format.CSV.extension()).isEqualTo("csv");
        assertThat(Format.CSV.isExcel()).isFalse();
        assertThat(Format.CSV.isCsv()).isTrue();
        assertThat(Format.CSV.isSpreadsheet()).isFalse();

        assertThat(Format.ODS.extension()).isEqualTo("ods");
        assertThat(Format.ODS.isExcel()).isFalse();
        assertThat(Format.ODS.isCsv()).isFalse();
        assertThat(Format.ODS.isOds()).isTrue();
        assertThat(Format.ODS.isSpreadsheet()).isTrue();
    }
}
