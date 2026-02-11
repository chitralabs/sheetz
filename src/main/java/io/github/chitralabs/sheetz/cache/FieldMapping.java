package io.github.chitralabs.sheetz.cache;

import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.convert.Converter;
import io.github.chitralabs.sheetz.exception.SheetzException;
import java.lang.reflect.Field;

/**
 * Immutable descriptor that pairs a Java {@link java.lang.reflect.Field}
 * with the spreadsheet-mapping metadata extracted from its optional
 * {@link Column} annotation.
 *
 * <p>Instances are created via {@link #from(java.lang.reflect.Field)} and
 * are cached by {@link MappingCache} so that reflection and annotation
 * processing happen only once per class.</p>
 *
 * @see Column
 * @see MappingCache
 */
public final class FieldMapping {
    private final Field field;
    private final String headerName;
    private final int columnIndex;
    private final String format;
    private final boolean required;
    private final String defaultValue;
    private final int width;
    private final Converter<?> converter;
    
    private FieldMapping(Field field, String headerName, int columnIndex, String format,
                         boolean required, String defaultValue, int width, Converter<?> converter) {
        this.field = field; this.headerName = headerName; this.columnIndex = columnIndex;
        this.format = format; this.required = required; this.defaultValue = defaultValue;
        this.width = width; this.converter = converter;
        this.field.setAccessible(true);
    }
    
    public static FieldMapping from(Field field) {
        Column ann = field.getAnnotation(Column.class);
        String name = field.getName();
        int index = -1; String format = ""; boolean required = false;
        String defaultValue = ""; int width = 0; Converter<?> converter = null;
        
        if (ann != null) {
            name = ann.value().isEmpty() ? field.getName() : ann.value();
            index = ann.index(); format = ann.format(); required = ann.required();
            defaultValue = ann.defaultValue(); width = ann.width();
            if (ann.converter() != Converter.None.class) {
                try { converter = ann.converter().getDeclaredConstructor().newInstance(); }
                catch (Exception e) {
                    throw new SheetzException("Failed to instantiate converter " + ann.converter().getName()
                        + " for field '" + field.getName() + "': " + e.getMessage(), e);
                }
            }
        }
        return new FieldMapping(field, name, index, format, required, defaultValue, width, converter);
    }
    
    public Field field() { return field; }
    public String headerName() { return headerName; }
    public int columnIndex() { return columnIndex; }
    public String format() { return format; }
    public boolean required() { return required; }
    public String defaultValue() { return defaultValue; }
    public int width() { return width; }
    public Converter<?> converter() { return converter; }
    public Class<?> type() { return field.getType(); }
    public boolean hasCustomConverter() { return converter != null; }
    public boolean hasDefaultValue() { return defaultValue != null && !defaultValue.isEmpty(); }
    public boolean hasExplicitIndex() { return columnIndex >= 0; }
    public Object getValue(Object obj) throws IllegalAccessException { return field.get(obj); }
    public void setValue(Object obj, Object value) throws IllegalAccessException { field.set(obj, value); }
}
