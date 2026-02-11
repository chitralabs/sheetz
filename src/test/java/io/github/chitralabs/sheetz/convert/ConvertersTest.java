package io.github.chitralabs.sheetz.convert;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for individual Converters with edge cases.
 */
class ConvertersTest {

    private ConvertContext ctx() {
        return ConvertContext.builder()
            .config(SheetzConfig.defaults())
            .fieldName("test")
            .targetType(String.class)
            .row(1).column("A")
            .build();
    }

    // ==================== String Converter ====================

    @Test void stringFromNull() {
        assertThat(Converters.get(String.class).fromCell(null, ctx())).isNull();
    }

    @Test void stringFromNumber() {
        assertThat(Converters.get(String.class).fromCell(42.5, ctx())).isEqualTo("42.5");
    }

    @Test void stringTrimsWhenConfigured() {
        assertThat(Converters.get(String.class).fromCell("  hello  ", ctx())).isEqualTo("hello");
    }

    @Test void stringNoTrimWhenDisabled() {
        ConvertContext noTrim = ConvertContext.builder().config(SheetzConfig.builder().trimValues(false).build())
            .fieldName("t").targetType(String.class).row(1).column("A").build();
        assertThat(Converters.get(String.class).fromCell("  hello  ", noTrim)).isEqualTo("  hello  ");
    }

    // ==================== Integer Converter ====================

    @Test void intFromBlank() {
        assertThat(Converters.get(Integer.class).fromCell("", ctx())).isNull();
        assertThat(Converters.get(Integer.class).fromCell(null, ctx())).isNull();
        assertThat(Converters.get(Integer.class).fromCell("  ", ctx())).isNull();
    }

    @Test void intFromNumber() {
        assertThat(Converters.get(Integer.class).fromCell(42.0, ctx())).isEqualTo(42);
    }

    @Test void intFromString() {
        assertThat(Converters.get(Integer.class).fromCell("42", ctx())).isEqualTo(42);
        assertThat(Converters.get(Integer.class).fromCell("42.7", ctx())).isEqualTo(42);
    }

    @Test void intFromInvalid() {
        assertThatThrownBy(() -> Converters.get(Integer.class).fromCell("abc", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    @Test void intPrimitive() {
        assertThat(Converters.get(int.class).fromCell(42.0, ctx())).isEqualTo(42);
    }

    // ==================== Long Converter ====================

    @Test void longFromLargeNumber() {
        assertThat(Converters.get(Long.class).fromCell(9999999999L, ctx())).isEqualTo(9999999999L);
    }

    @Test void longFromString() {
        assertThat(Converters.get(Long.class).fromCell("123456789012", ctx())).isEqualTo(123456789012L);
    }

    // ==================== Double Converter ====================

    @Test void doubleFromString() {
        assertThat(Converters.get(Double.class).fromCell("3.14", ctx())).isEqualTo(3.14);
    }

    @Test void doubleFromInvalid() {
        assertThatThrownBy(() -> Converters.get(Double.class).fromCell("not-a-number", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    // ==================== Float Converter ====================

    @Test void floatFromString() {
        assertThat(Converters.get(Float.class).fromCell("2.5", ctx())).isEqualTo(2.5f);
    }

    // ==================== Short Converter ====================

    @Test void shortFromString() {
        assertThat(Converters.get(Short.class).fromCell("32767", ctx())).isEqualTo((short) 32767);
        assertThat(Converters.get(Short.class).fromCell("-32768", ctx())).isEqualTo((short) -32768);
    }

    // ==================== Byte Converter ====================

    @Test void byteFromString() {
        assertThat(Converters.get(Byte.class).fromCell("127", ctx())).isEqualTo((byte) 127);
        assertThat(Converters.get(Byte.class).fromCell("-128", ctx())).isEqualTo((byte) -128);
    }

    // ==================== BigDecimal Converter ====================

    @Test void bigDecimalFromString() {
        assertThat(Converters.get(BigDecimal.class).fromCell("99.99", ctx())).isEqualByComparingTo("99.99");
    }

    @Test void bigDecimalFromNumber() {
        assertThat(Converters.get(BigDecimal.class).fromCell(42.5, ctx())).isEqualByComparingTo("42.5");
    }

    @Test void bigDecimalFromBigDecimal() {
        BigDecimal bd = new BigDecimal("123.456");
        assertThat(Converters.get(BigDecimal.class).fromCell(bd, ctx())).isSameAs(bd);
    }

    @Test void bigDecimalToCell() {
        assertThat(Converters.get(BigDecimal.class).toCell(new BigDecimal("99.99"))).isEqualTo(99.99);
    }

    @Test void bigDecimalFromInvalid() {
        assertThatThrownBy(() -> Converters.get(BigDecimal.class).fromCell("abc", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    // ==================== BigInteger Converter ====================

    @Test void bigIntegerFromString() {
        assertThat(Converters.get(BigInteger.class).fromCell("1234567890", ctx())).isEqualTo(BigInteger.valueOf(1234567890));
    }

    @Test void bigIntegerFromFloat() {
        // Should truncate decimal
        assertThat(Converters.get(BigInteger.class).fromCell("42.9", ctx())).isEqualTo(BigInteger.valueOf(42));
    }

    // ==================== Boolean Converter ====================

    @Test void boolAllTrueValues() {
        for (String v : new String[]{"true", "TRUE", "yes", "YES", "y", "Y", "1", "on", "ON"}) {
            assertThat(Converters.get(Boolean.class).fromCell(v, ctx())).as("Should be true: " + v).isTrue();
        }
    }

    @Test void boolAllFalseValues() {
        for (String v : new String[]{"false", "FALSE", "no", "NO", "n", "N", "0", "off", "OFF"}) {
            assertThat(Converters.get(Boolean.class).fromCell(v, ctx())).as("Should be false: " + v).isFalse();
        }
    }

    @Test void boolFromBooleanObject() {
        assertThat(Converters.get(Boolean.class).fromCell(Boolean.TRUE, ctx())).isTrue();
    }

    @Test void boolFromInvalid() {
        assertThatThrownBy(() -> Converters.get(Boolean.class).fromCell("maybe", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    @Test void boolFromNull() {
        assertThat(Converters.get(Boolean.class).fromCell(null, ctx())).isNull();
    }

    @Test void boolFromEmptyString() {
        assertThat(Converters.get(Boolean.class).fromCell("", ctx())).isNull();
    }

    // ==================== Character Converter ====================

    @Test void charFromString() {
        assertThat(Converters.get(Character.class).fromCell("A", ctx())).isEqualTo('A');
    }

    @Test void charFromMultiCharTakesFirst() {
        assertThat(Converters.get(Character.class).fromCell("Hello", ctx())).isEqualTo('H');
    }

    @Test void charFromBlank() {
        assertThat(Converters.get(Character.class).fromCell("", ctx())).isNull();
    }

    @Test void charToCell() {
        assertThat(Converters.get(Character.class).toCell('X')).isEqualTo("X");
    }

    // ==================== LocalDate Converter ====================

    @Test void localDateFromDate() {
        Date d = Date.from(LocalDate.of(2024, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertThat(Converters.get(LocalDate.class).fromCell(d, ctx())).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test void localDateFromLocalDateTime() {
        assertThat(Converters.get(LocalDate.class).fromCell(LocalDateTime.of(2024, 1, 15, 10, 30), ctx()))
            .isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test void localDateFromISOString() {
        assertThat(Converters.get(LocalDate.class).fromCell("2024-01-15", ctx())).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test void localDateFromCustomFormat() {
        ConvertContext customCtx = ConvertContext.builder().config(SheetzConfig.defaults())
            .format("dd/MM/yyyy").fieldName("d").targetType(LocalDate.class).row(1).column("A").build();
        assertThat(Converters.get(LocalDate.class).fromCell("15/01/2024", customCtx)).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test void localDateFromInvalid() {
        assertThatThrownBy(() -> Converters.get(LocalDate.class).fromCell("not-a-date", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    // ==================== LocalDateTime Converter ====================

    @Test void localDateTimeFromISO() {
        assertThat(Converters.get(LocalDateTime.class).fromCell("2024-01-15T10:30:00", ctx()))
            .isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    @Test void localDateTimeFromLocalDate() {
        assertThat(Converters.get(LocalDateTime.class).fromCell(LocalDate.of(2024, 1, 15), ctx()))
            .isEqualTo(LocalDateTime.of(2024, 1, 15, 0, 0));
    }

    // ==================== LocalTime Converter ====================

    @Test void localTimeFromISO() {
        assertThat(Converters.get(LocalTime.class).fromCell("10:30:00", ctx())).isEqualTo(LocalTime.of(10, 30));
    }

    @Test void localTimeFromInvalid() {
        assertThatThrownBy(() -> Converters.get(LocalTime.class).fromCell("not-a-time", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    // ==================== Instant Converter ====================

    @Test void instantFromISO() {
        assertThat(Converters.get(Instant.class).fromCell("2024-01-15T10:30:00Z", ctx()))
            .isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
    }

    @Test void instantFromDate() {
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        assertThat(Converters.get(Instant.class).fromCell(Date.from(now), ctx())).isEqualTo(now);
    }

    @Test void instantFromInvalid() {
        assertThatThrownBy(() -> Converters.get(Instant.class).fromCell("not-an-instant", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    // ==================== ZonedDateTime Converter ====================

    @Test void zonedDateTimeFromString() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneId.of("Europe/Paris"));
        Object result = Converters.get(ZonedDateTime.class).fromCell(zdt.toString(), ctx());
        assertThat(((ZonedDateTime) result).toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    @Test void zonedDateTimeFromInstant() {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        ZonedDateTime result = (ZonedDateTime) Converters.get(ZonedDateTime.class).fromCell(instant, ctx());
        assertThat(result.toInstant()).isEqualTo(instant);
    }

    // ==================== UUID Converter ====================

    @Test void uuidFromString() {
        UUID uuid = UUID.randomUUID();
        assertThat(Converters.get(UUID.class).fromCell(uuid.toString(), ctx())).isEqualTo(uuid);
    }

    @Test void uuidFromInvalid() {
        assertThatThrownBy(() -> Converters.get(UUID.class).fromCell("not-a-uuid", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    @Test void uuidToCell() {
        UUID uuid = UUID.randomUUID();
        assertThat(Converters.get(UUID.class).toCell(uuid)).isEqualTo(uuid.toString());
    }

    // ==================== Enum Converter ====================

    @Test void enumExactMatch() {
        Converters.reset(); // Clear to trigger auto-detection
        @SuppressWarnings("unchecked")
        Converter<TestEnum> conv = Converters.get(TestEnum.class);
        assertThat(conv.fromCell("ACTIVE", ctx())).isEqualTo(TestEnum.ACTIVE);
    }

    @Test void enumCaseInsensitive() {
        @SuppressWarnings("unchecked")
        Converter<TestEnum> conv = Converters.get(TestEnum.class);
        assertThat(conv.fromCell("inactive", ctx())).isEqualTo(TestEnum.INACTIVE);
        assertThat(conv.fromCell("Pending", ctx())).isEqualTo(TestEnum.PENDING);
    }

    @Test void enumInvalid() {
        @SuppressWarnings("unchecked")
        Converter<TestEnum> conv = Converters.get(TestEnum.class);
        assertThatThrownBy(() -> conv.fromCell("UNKNOWN", ctx()))
            .isInstanceOf(SheetzException.class);
    }

    @Test void enumToCell() {
        @SuppressWarnings("unchecked")
        Converter<TestEnum> conv = Converters.get(TestEnum.class);
        assertThat(conv.toCell(TestEnum.ACTIVE)).isEqualTo("ACTIVE");
        assertThat(conv.toCell(null)).isNull();
    }

    // ==================== Registration ====================

    @Test void testRegisterAndGet() {
        Converters.register(CustomType.class, new CustomConverter());
        assertThat(Converters.has(CustomType.class)).isTrue();
        assertThat(Converters.get(CustomType.class)).isNotNull();
    }

    @Test void testReset() {
        Converters.register(CustomType.class, new CustomConverter());
        Converters.reset();
        assertThat(Converters.has(CustomType.class)).isFalse();
        // Built-in converters still present
        assertThat(Converters.has(String.class)).isTrue();
        assertThat(Converters.has(Integer.class)).isTrue();
    }

    @Test void testHasEnum() {
        assertThat(Converters.has(TestEnum.class)).isTrue();
    }

    // Test types
    public enum TestEnum { ACTIVE, INACTIVE, PENDING }
    public static class CustomType { String value; }
    public static class CustomConverter implements Converter<CustomType> {
        public CustomType fromCell(Object v, ConvertContext ctx) { CustomType ct = new CustomType(); ct.value = v.toString(); return ct; }
        public Object toCell(CustomType v) { return v.value; }
    }
}
