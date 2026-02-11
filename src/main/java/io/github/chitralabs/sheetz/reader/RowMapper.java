package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared row-mapping utility used by all readers (ExcelReader, CsvReader, StreamingReader).
 *
 * <p>Centralizes the logic for resolving field mappings from headers and converting
 * cell values to Java objects. This eliminates the duplication that previously existed
 * across three separate reader implementations.</p>
 */
final class RowMapper {

    private RowMapper() {}

    /**
     * A resolved mapping between a FieldMapping and its column index in the source data.
     */
    static final class ResolvedField {
        final FieldMapping mapping;
        final int colIdx;
        ResolvedField(FieldMapping mapping, int colIdx) {
            this.mapping = mapping;
            this.colIdx = colIdx;
        }
    }

    /**
     * Resolves which columns in the source data map to which fields in the target class.
     *
     * @param classMapping the class metadata
     * @param resolver the column name resolver
     * @return list of resolved field-to-column mappings
     */
    static List<ResolvedField> resolveFields(MappingCache.ClassMapping classMapping, ColumnResolver resolver) {
        List<ResolvedField> resolved = new ArrayList<>();
        for (FieldMapping fm : classMapping.fields()) {
            int colIdx = fm.hasExplicitIndex() ? fm.columnIndex() : resolver.resolve(fm.headerName());
            if (colIdx >= 0) {
                resolved.add(new ResolvedField(fm, colIdx));
            }
        }
        return resolved;
    }

    /**
     * Maps a string array (CSV row) to a Java object.
     *
     * @param <T> the target type
     * @param classMapping the class metadata
     * @param fields the resolved field mappings
     * @param line the string array from the CSV row
     * @param rowNum the 1-based row number for error reporting
     * @param headers the header names
     * @param config the current configuration
     * @return the mapped object, or null if the row should be skipped
     */
    @SuppressWarnings("unchecked")
    static <T> T mapStringArray(MappingCache.ClassMapping classMapping, List<ResolvedField> fields,
                                 String[] line, int rowNum, List<String> headers, SheetzConfig config) {
        try {
            T obj = classMapping.newInstance();
            for (ResolvedField rf : fields) {
                FieldMapping fm = rf.mapping;
                if (rf.colIdx >= line.length) continue;

                String colName = rf.colIdx < headers.size() ? headers.get(rf.colIdx) : "Column " + rf.colIdx;
                String cellValue = line[rf.colIdx];

                if (config.trimValues() && cellValue != null) {
                    cellValue = cellValue.trim();
                }
                if (fm.required() && (cellValue == null || cellValue.isEmpty())) {
                    throw new MappingException("Required field is empty", rowNum, colName, cellValue);
                }
                if ((cellValue == null || cellValue.isEmpty()) && fm.hasDefaultValue()) {
                    cellValue = fm.defaultValue();
                }
                if (cellValue != null && !cellValue.isEmpty()) {
                    ConvertContext ctx = ConvertContext.builder()
                        .fieldName(fm.field().getName())
                        .targetType(fm.type())
                        .format(fm.format())
                        .config(config)
                        .row(rowNum)
                        .column(colName)
                        .build();

                    Object converted;
                    if (fm.hasCustomConverter()) {
                        converted = ((Converter<Object>) fm.converter()).fromCell(cellValue, ctx);
                    } else {
                        Converter<?> conv = Converters.get(fm.type());
                        converted = conv != null ? ((Converter<Object>) conv).fromCell(cellValue, ctx) : cellValue;
                    }
                    fm.setValue(obj, converted);
                }
            }
            return obj;
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Failed to map row: " + e.getMessage(), rowNum);
        }
    }

    /**
     * Maps a Map&lt;Integer, String&gt; (streaming XLSX row) to a Java object.
     *
     * @param <T> the target type
     * @param classMapping the class metadata
     * @param fields the resolved field mappings
     * @param data the map of column-index to cell-value
     * @param rowNum the 1-based row number for error reporting
     * @param headers the header names
     * @param config the current configuration
     * @return the mapped object, or null if the row should be skipped
     */
    @SuppressWarnings("unchecked")
    static <T> T mapStringRow(MappingCache.ClassMapping classMapping, List<ResolvedField> fields,
                               Map<Integer, String> data, int rowNum, List<String> headers, SheetzConfig config) {
        if (data.isEmpty() && config.skipEmptyRows()) return null;

        try {
            T obj = classMapping.newInstance();
            for (ResolvedField rf : fields) {
                FieldMapping fm = rf.mapping;
                String colName = rf.colIdx < headers.size() ? headers.get(rf.colIdx) : "Column " + rf.colIdx;
                String cellValue = data.get(rf.colIdx);

                if (config.trimValues() && cellValue != null) {
                    cellValue = cellValue.trim();
                }
                if (fm.required() && (cellValue == null || cellValue.isEmpty())) {
                    throw new MappingException("Required field is empty", rowNum, colName, cellValue);
                }
                if ((cellValue == null || cellValue.isEmpty()) && fm.hasDefaultValue()) {
                    cellValue = fm.defaultValue();
                }
                if (cellValue != null && !cellValue.isEmpty()) {
                    ConvertContext ctx = ConvertContext.builder()
                        .fieldName(fm.field().getName())
                        .targetType(fm.type())
                        .format(fm.format())
                        .config(config)
                        .row(rowNum)
                        .column(colName)
                        .build();

                    Object converted;
                    if (fm.hasCustomConverter()) {
                        converted = ((Converter<Object>) fm.converter()).fromCell(cellValue, ctx);
                    } else {
                        Converter<?> conv = Converters.get(fm.type());
                        converted = conv != null ? ((Converter<Object>) conv).fromCell(cellValue, ctx) : cellValue;
                    }
                    fm.setValue(obj, converted);
                }
            }
            return obj;
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Failed to map row: " + e.getMessage(), rowNum);
        }
    }

    /**
     * Converts cell values to strings for the purpose of type conversion.
     * Used by readers that deal with generic Object cell values (Excel).
     *
     * @param <T> the target type
     * @param classMapping the class metadata
     * @param fields the resolved field mappings
     * @param getCellValue function to retrieve cell values by column index
     * @param rowNum the 1-based row number
     * @param headers the header names
     * @param config the current configuration
     * @return the mapped object
     */
    @SuppressWarnings("unchecked")
    static <T> T mapObjectRow(MappingCache.ClassMapping classMapping, List<ResolvedField> fields,
                               java.util.function.IntFunction<Object> getCellValue, int rowNum,
                               List<String> headers, SheetzConfig config) {
        try {
            T obj = classMapping.newInstance();
            for (ResolvedField rf : fields) {
                FieldMapping fm = rf.mapping;
                String colName = rf.colIdx < headers.size() ? headers.get(rf.colIdx) : "Column " + rf.colIdx;
                Object cellValue = getCellValue.apply(rf.colIdx);

                if (fm.required() && isBlank(cellValue)) {
                    throw new MappingException("Required field is empty", rowNum, colName, cellValue);
                }
                if (isBlank(cellValue) && fm.hasDefaultValue()) {
                    cellValue = fm.defaultValue();
                }
                if (!isBlank(cellValue)) {
                    ConvertContext ctx = ConvertContext.builder()
                        .fieldName(fm.field().getName())
                        .targetType(fm.type())
                        .format(fm.format())
                        .config(config)
                        .row(rowNum)
                        .column(colName)
                        .build();

                    Object converted;
                    if (fm.hasCustomConverter()) {
                        converted = ((Converter<Object>) fm.converter()).fromCell(cellValue, ctx);
                    } else {
                        Converter<?> conv = Converters.get(fm.type());
                        converted = conv != null ? ((Converter<Object>) conv).fromCell(cellValue, ctx) :
                            (fm.type().isInstance(cellValue) ? cellValue : cellValue);
                    }
                    fm.setValue(obj, converted);
                }
            }
            return obj;
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Failed to map row: " + e.getMessage(), rowNum);
        }
    }

    private static boolean isBlank(Object v) {
        return v == null || v.toString().trim().isEmpty();
    }
}
