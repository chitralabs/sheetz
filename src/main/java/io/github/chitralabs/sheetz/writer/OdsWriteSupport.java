package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.cache.FieldMapping;
import io.github.chitralabs.sheetz.convert.Converter;
import io.github.chitralabs.sheetz.convert.Converters;
import io.github.chitralabs.sheetz.style.HyperlinkValue;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

/**
 * Shared utility methods for writing ODS cell values.
 */
final class OdsWriteSupport {

    private OdsWriteSupport() {}

    @SuppressWarnings("unchecked")
    static void writeRow(Object obj, OdfTable table, int rowIndex, List<FieldMapping> fields) {
        int col = 0;
        for (FieldMapping fm : fields) {
            try {
                Object value = fm.getValue(obj);
                if (value == null) {
                    col++;
                    continue;
                }

                // Handle HyperlinkValue
                if (value instanceof HyperlinkValue) {
                    HyperlinkValue hv = (HyperlinkValue) value;
                    setCellValue(table, rowIndex, col, hv.displayText());
                    col++;
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
                setCellValue(table, rowIndex, col, cellValue);
            } catch (IllegalAccessException e) {
                // Leave cell empty
            }
            col++;
        }
    }

    static void setCellValue(OdfTable table, int row, int col, Object value) {
        OdfTableCell cell = table.getCellByPosition(col, row);
        if (cell == null) return;
        if (value == null) {
            return; // Leave blank
        } else if (value instanceof String) {
            cell.setStringValue((String) value);
        } else if (value instanceof Number) {
            cell.setDoubleValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setBooleanValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((java.util.Date) value);
            cell.setDateValue(cal);
        } else if (value instanceof LocalDate) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(java.util.Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cell.setDateValue(cal);
        } else if (value instanceof LocalDateTime) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(java.util.Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()));
            cell.setDateValue(cal);
        } else {
            cell.setStringValue(value.toString());
        }
    }
}
