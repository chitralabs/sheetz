package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.SheetzConfig;
import io.github.chitralabs.sheetz.cache.FieldMapping;
import io.github.chitralabs.sheetz.convert.Converter;
import io.github.chitralabs.sheetz.convert.Converters;
import io.github.chitralabs.sheetz.style.CellStyleDef;
import io.github.chitralabs.sheetz.style.HyperlinkValue;
import io.github.chitralabs.sheetz.style.PoiStyleResolver;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Shared helper methods for writing Excel cell values, creating header
 * styles, and creating date styles. Used internally by both
 * {@link ExcelWriter} and {@link WorkbookBuilder} so that the formatting
 * logic lives in one place instead of being duplicated.
 */
final class ExcelWriteSupport {

    private ExcelWriteSupport() {}

    /**
     * Writes a single data row into the given POI {@link Row}.
     *
     * <p>Each field mapping is resolved through its converter (custom or
     * built-in) and the resulting value is written into the corresponding
     * cell. Fields that cannot be accessed are written as blank.</p>
     *
     * @param obj       the source object whose field values are read
     * @param row       the POI row to populate
     * @param fields    ordered list of field mappings that drive the column layout
     * @param dateStyle cell style applied to date and date-time values
     */
    @SuppressWarnings("unchecked")
    static void writeRow(Object obj, Row row, List<FieldMapping> fields, CellStyle dateStyle) {
        int col = 0;
        for (FieldMapping fm : fields) {
            Cell cell = row.createCell(col++);
            try {
                Object value = fm.getValue(obj);
                if (value == null) {
                    cell.setBlank();
                    continue;
                }
                Object cellValue = value;
                if (fm.hasCustomConverter()) {
                    cellValue = ((Converter<Object>) fm.converter()).toCell(value);
                } else {
                    Converter<?> conv = Converters.get(value.getClass());
                    if (conv != null) {
                        @SuppressWarnings("rawtypes")
                        Object c = ((Converter) conv).toCell(value);
                        cellValue = c;
                    }
                }
                setCellValue(cell, cellValue, dateStyle);
            } catch (IllegalAccessException e) {
                cell.setBlank();
            }
        }
    }

    /**
     * Writes a single data row with per-column styles applied.
     *
     * @param obj           the source object
     * @param row           the POI row to populate
     * @param fields        ordered list of field mappings
     * @param dateStyle     cell style for date values
     * @param styleResolver resolver for annotation-based styles (may be null)
     */
    @SuppressWarnings("unchecked")
    static void writeRow(Object obj, Row row, List<FieldMapping> fields,
                         CellStyle dateStyle, PoiStyleResolver styleResolver) {
        int col = 0;
        for (FieldMapping fm : fields) {
            Cell cell = row.createCell(col++);
            try {
                Object value = fm.getValue(obj);
                if (value == null) {
                    cell.setBlank();
                    continue;
                }

                // Handle HyperlinkValue type
                if (value instanceof HyperlinkValue) {
                    HyperlinkValue hv = (HyperlinkValue) value;
                    cell.setCellValue(hv.displayText());
                    CreationHelper helper = row.getSheet().getWorkbook().getCreationHelper();
                    Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
                    link.setAddress(hv.url());
                    cell.setHyperlink(link);
                    if (styleResolver != null && fm.hasStyle()) {
                        CellStyle resolved = styleResolver.resolve(fm.styleDef());
                        if (resolved != null) cell.setCellStyle(resolved);
                    }
                    continue;
                }

                Object cellValue = value;
                if (fm.hasCustomConverter()) {
                    cellValue = ((Converter<Object>) fm.converter()).toCell(value);
                } else {
                    Converter<?> conv = Converters.get(value.getClass());
                    if (conv != null) {
                        @SuppressWarnings("rawtypes")
                        Object c = ((Converter) conv).toCell(value);
                        cellValue = c;
                    }
                }
                setCellValue(cell, cellValue, dateStyle);

                // Apply @Style annotation if present
                if (styleResolver != null && fm.hasStyle()) {
                    CellStyleDef styleDef = fm.styleDef();
                    CellStyle resolved = styleResolver.resolve(styleDef);
                    if (resolved != null) cell.setCellStyle(resolved);
                }

                // Handle hyperlink via @Style(hyperlink = true)
                if (fm.hasStyle() && fm.styleDef().hyperlink() && cellValue instanceof String) {
                    String url = (String) cellValue;
                    CreationHelper helper = row.getSheet().getWorkbook().getCreationHelper();
                    HyperlinkType hlType = parseHyperlinkType(fm.styleDef().hyperlinkType());
                    Hyperlink link = helper.createHyperlink(hlType);
                    link.setAddress(url);
                    cell.setHyperlink(link);
                }
            } catch (IllegalAccessException e) {
                cell.setBlank();
            }
        }
    }

    private static HyperlinkType parseHyperlinkType(String type) {
        if (type == null || type.isEmpty()) return HyperlinkType.URL;
        switch (type.toUpperCase()) {
            case "URL": return HyperlinkType.URL;
            case "DOCUMENT": return HyperlinkType.DOCUMENT;
            case "EMAIL": return HyperlinkType.EMAIL;
            case "FILE": return HyperlinkType.FILE;
            default: return HyperlinkType.URL;
        }
    }

    /**
     * Sets a POI cell value based on the runtime type of the supplied
     * object. Dates and date-time values receive the provided date style
     * so that they render correctly in spreadsheet applications.
     *
     * @param cell      the target cell
     * @param value     the value to write (may be {@code null})
     * @param dateStyle cell style for temporal values
     */
    static void setCellValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(java.util.Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(java.util.Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Creates a bold header style with a light-grey background fill.
     *
     * @param wb the workbook that owns the style
     * @return a reusable cell style for header rows
     */
    static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a date cell style that uses the date format from the
     * supplied configuration.
     *
     * @param wb     the workbook that owns the style
     * @param config configuration supplying the date format pattern
     * @return a reusable cell style for date values
     */
    static CellStyle createDateStyle(Workbook wb, SheetzConfig config) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(config.dateFormat()));
        return style;
    }
}
