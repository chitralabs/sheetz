package io.github.chitralabs.sheetz.convert;

import io.github.chitralabs.sheetz.exception.SheetzException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of type converters for cell value conversion.
 *
 * <p>Provides 19 built-in converters for common Java types including primitives,
 * date/time types, and auto-detected enums. Thread-safe for concurrent access.</p>
 *
 * <p>Custom converters can be registered globally:</p>
 * <pre>{@code
 * Converters.register(Money.class, new MoneyConverter());
 * }</pre>
 *
 * @see Converter
 * @see ConvertContext
 */
public final class Converters {
    private static final Map<Class<?>, Converter<?>> REGISTRY = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    static { registerDefaults(); }
    private Converters() {}

    private static void registerDefaults() {
        // String
        register(String.class, new StringConv());

        // Integer types
        register(Integer.class, new IntConv()); register(int.class, new IntConv());
        register(Long.class, new LongConv()); register(long.class, new LongConv());
        register(Short.class, new ShortConv()); register(short.class, new ShortConv());
        register(Byte.class, new ByteConv()); register(byte.class, new ByteConv());

        // Floating point types
        register(Double.class, new DoubleConv()); register(double.class, new DoubleConv());
        register(Float.class, new FloatConv()); register(float.class, new FloatConv());

        // Big number types
        register(BigDecimal.class, new BigDecimalConv());
        register(BigInteger.class, new BigIntegerConv());

        // Boolean and Character
        register(Boolean.class, new BoolConv()); register(boolean.class, new BoolConv());
        register(Character.class, new CharConv()); register(char.class, new CharConv());

        // Date/Time types
        register(LocalDate.class, new LocalDateConv());
        register(LocalDateTime.class, new LocalDateTimeConv());
        register(LocalTime.class, new LocalTimeConv());
        register(Instant.class, new InstantConv());
        register(ZonedDateTime.class, new ZonedDateTimeConv());
        register(Date.class, new DateConv());

        // Other types
        register(UUID.class, new UUIDConv());
    }

    public static <T> void register(Class<T> type, Converter<T> conv) {
        Objects.requireNonNull(type); Objects.requireNonNull(conv);
        synchronized (LOCK) {
            REGISTRY.put(type, conv);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Converter<T> get(Class<T> type) {
        Converter<T> conv = (Converter<T>) REGISTRY.get(type);
        if (conv == null && type.isEnum()) {
            synchronized (LOCK) {
                conv = (Converter<T>) REGISTRY.get(type);
                if (conv == null) {
                    conv = (Converter<T>) new EnumConv<>((Class<Enum>) type);
                    REGISTRY.put(type, conv);
                }
            }
        }
        return conv;
    }

    public static boolean has(Class<?> type) { return REGISTRY.containsKey(type) || type.isEnum(); }
    public static void reset() { synchronized (LOCK) { REGISTRY.clear(); registerDefaults(); } }

    private static boolean isBlank(Object v) { return v == null || v.toString().trim().isEmpty(); }

    static final class StringConv implements Converter<String> {
                @Override
        public String fromCell(Object v, ConvertContext ctx) {
            if (v == null) return null;
            String s = v.toString();
            return (ctx.config() != null && ctx.config().trimValues()) ? s.trim() : s;
        }
                @Override
        public Object toCell(String v) { return v; }
    }

    static final class IntConv implements Converter<Integer> {
                @Override
        public Integer fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).intValue();
            try { return (int) Double.parseDouble(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Integer: " + v); }
        }
                @Override
        public Object toCell(Integer v) { return v; }
    }

    static final class LongConv implements Converter<Long> {
                @Override
        public Long fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).longValue();
            try { return (long) Double.parseDouble(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Long: " + v); }
        }
                @Override
        public Object toCell(Long v) { return v; }
    }

    static final class DoubleConv implements Converter<Double> {
                @Override
        public Double fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).doubleValue();
            try { return Double.parseDouble(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Double: " + v); }
        }
                @Override
        public Object toCell(Double v) { return v; }
    }

    static final class FloatConv implements Converter<Float> {
                @Override
        public Float fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).floatValue();
            try { return Float.parseFloat(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Float: " + v); }
        }
                @Override
        public Object toCell(Float v) { return v; }
    }

    static final class BigDecimalConv implements Converter<BigDecimal> {
                @Override
        public BigDecimal fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof BigDecimal) return (BigDecimal) v;
            if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
            try { return new BigDecimal(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to BigDecimal: " + v); }
        }
                @Override
        public Object toCell(BigDecimal v) { return v != null ? v.doubleValue() : null; }
    }

    static final class BigIntegerConv implements Converter<BigInteger> {
                @Override
        public BigInteger fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof BigInteger) return (BigInteger) v;
            if (v instanceof Number) return BigInteger.valueOf(((Number) v).longValue());
            try { return new BigInteger(v.toString().trim().split("\\.")[0]); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to BigInteger: " + v); }
        }
                @Override
        public Object toCell(BigInteger v) { return v != null ? v.longValue() : null; }
    }

    static final class BoolConv implements Converter<Boolean> {
                @Override
        public Boolean fromCell(Object v, ConvertContext ctx) {
            if (v == null) return null;
            if (v instanceof Boolean) return (Boolean) v;
            String s = v.toString().trim().toLowerCase();
            if (s.isEmpty()) return null;
            if ("true".equals(s) || "yes".equals(s) || "y".equals(s) || "1".equals(s) || "on".equals(s)) {
                return true;
            } else if ("false".equals(s) || "no".equals(s) || "n".equals(s) || "0".equals(s) || "off".equals(s)) {
                return false;
            } else {
                throw new SheetzException("Cannot convert to Boolean: " + v);
            }
        }
                @Override
        public Object toCell(Boolean v) { return v; }
    }

    static final class LocalDateConv implements Converter<LocalDate> {
                @Override
        public LocalDate fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof LocalDate) return (LocalDate) v;
            if (v instanceof LocalDateTime) return ((LocalDateTime) v).toLocalDate();
            if (v instanceof Date) return ((Date) v).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String s = v.toString().trim();
            String fmt = ctx.formatOr(ctx.config() != null ? ctx.config().dateFormat() : "yyyy-MM-dd");
            try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(fmt)); }
            catch (DateTimeParseException e) {
                try { return LocalDate.parse(s); }
                catch (DateTimeParseException e2) { throw new SheetzException("Cannot convert to LocalDate: " + v); }
            }
        }
                @Override
        public Object toCell(LocalDate v) { return v; }
    }

    static final class LocalDateTimeConv implements Converter<LocalDateTime> {
                @Override
        public LocalDateTime fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof LocalDateTime) return (LocalDateTime) v;
            if (v instanceof LocalDate) return ((LocalDate) v).atStartOfDay();
            if (v instanceof Date) return ((Date) v).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String s = v.toString().trim();
            String fmt = ctx.formatOr(ctx.config() != null ? ctx.config().dateTimeFormat() : "yyyy-MM-dd HH:mm:ss");
            try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(fmt)); }
            catch (DateTimeParseException e) {
                try { return LocalDateTime.parse(s); }
                catch (DateTimeParseException e2) { throw new SheetzException("Cannot convert to LocalDateTime: " + v); }
            }
        }
                @Override
        public Object toCell(LocalDateTime v) { return v; }
    }

    static final class LocalTimeConv implements Converter<LocalTime> {
                @Override
        public LocalTime fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof LocalTime) return (LocalTime) v;
            if (v instanceof LocalDateTime) return ((LocalDateTime) v).toLocalTime();
            try { return LocalTime.parse(v.toString().trim()); }
            catch (DateTimeParseException e) { throw new SheetzException("Cannot convert to LocalTime: " + v); }
        }
                @Override
        public Object toCell(LocalTime v) { return v != null ? v.toString() : null; }
    }

    static final class DateConv implements Converter<Date> {
                @Override
        public Date fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Date) return (Date) v;
            LocalDateTime ldt = new LocalDateTimeConv().fromCell(v, ctx);
            return ldt != null ? Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()) : null;
        }
                @Override
        public Object toCell(Date v) { return v; }
    }

    /** Converter for Short/short values. */
    static final class ShortConv implements Converter<Short> {
                @Override
        public Short fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).shortValue();
            try { return (short) Double.parseDouble(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Short: " + v); }
        }
                @Override
        public Object toCell(Short v) { return v; }
    }

    /** Converter for Byte/byte values. */
    static final class ByteConv implements Converter<Byte> {
                @Override
        public Byte fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Number) return ((Number) v).byteValue();
            try { return (byte) Double.parseDouble(v.toString().trim()); }
            catch (NumberFormatException e) { throw new SheetzException("Cannot convert to Byte: " + v); }
        }
                @Override
        public Object toCell(Byte v) { return v; }
    }

    /** Converter for Character/char values. Takes first character of string. */
    static final class CharConv implements Converter<Character> {
                @Override
        public Character fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            String s = v.toString();
            if (s.isEmpty()) return null;
            return s.charAt(0);
        }
                @Override
        public Object toCell(Character v) { return v != null ? v.toString() : null; }
    }

    /** Converter for Instant values (UTC timestamps). */
    static final class InstantConv implements Converter<Instant> {
                @Override
        public Instant fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof Instant) return (Instant) v;
            if (v instanceof Date) return ((Date) v).toInstant();
            if (v instanceof LocalDateTime) return ((LocalDateTime) v).atZone(ZoneId.systemDefault()).toInstant();
            if (v instanceof ZonedDateTime) return ((ZonedDateTime) v).toInstant();
            String s = v.toString().trim();
            try { return Instant.parse(s); }
            catch (DateTimeParseException e) {
                // Try parsing as LocalDateTime and convert
                try {
                    LocalDateTime ldt = LocalDateTime.parse(s);
                    return ldt.atZone(ZoneId.systemDefault()).toInstant();
                } catch (DateTimeParseException e2) {
                    throw new SheetzException("Cannot convert to Instant: " + v);
                }
            }
        }
                @Override
        public Object toCell(Instant v) { return v != null ? v.toString() : null; }
    }

    /** Converter for ZonedDateTime values (timestamps with timezone). */
    static final class ZonedDateTimeConv implements Converter<ZonedDateTime> {
                @Override
        public ZonedDateTime fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof ZonedDateTime) return (ZonedDateTime) v;
            if (v instanceof Instant) return ((Instant) v).atZone(ZoneId.systemDefault());
            if (v instanceof Date) return ((Date) v).toInstant().atZone(ZoneId.systemDefault());
            if (v instanceof LocalDateTime) return ((LocalDateTime) v).atZone(ZoneId.systemDefault());
            String s = v.toString().trim();
            try { return ZonedDateTime.parse(s); }
            catch (DateTimeParseException e) {
                // Try parsing as LocalDateTime and add system timezone
                try {
                    LocalDateTime ldt = LocalDateTime.parse(s);
                    return ldt.atZone(ZoneId.systemDefault());
                } catch (DateTimeParseException e2) {
                    throw new SheetzException("Cannot convert to ZonedDateTime: " + v);
                }
            }
        }
                @Override
        public Object toCell(ZonedDateTime v) { return v != null ? v.toString() : null; }
    }

    /** Converter for UUID values. */
    static final class UUIDConv implements Converter<UUID> {
                @Override
        public UUID fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            if (v instanceof UUID) return (UUID) v;
            try { return UUID.fromString(v.toString().trim()); }
            catch (IllegalArgumentException e) { throw new SheetzException("Cannot convert to UUID: " + v); }
        }
                @Override
        public Object toCell(UUID v) { return v != null ? v.toString() : null; }
    }

    /** Converter for Enum values with case-insensitive matching. */
    static final class EnumConv<E extends Enum<E>> implements Converter<E> {
        private final Class<E> type;
        EnumConv(Class<E> type) { this.type = type; }
                @Override
        public E fromCell(Object v, ConvertContext ctx) {
            if (isBlank(v)) return null;
            String s = v.toString().trim();
            try { return Enum.valueOf(type, s); }
            catch (IllegalArgumentException e) {
                for (E c : type.getEnumConstants()) if (c.name().equalsIgnoreCase(s)) return c;
                throw new SheetzException("Cannot convert '" + s + "' to " + type.getSimpleName());
            }
        }
                @Override
        public Object toCell(E v) { return v != null ? v.name() : null; }
    }
}
