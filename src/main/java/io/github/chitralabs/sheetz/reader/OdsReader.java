package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.exception.*;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reader for ODS (OpenDocument Spreadsheet) files using ODF Toolkit.
 *
 * @param <T> the type to map rows to
 */
public final class OdsReader<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private int sheetIndex = 0;
    private String sheetName;
    private int headerRow = -1;

    public OdsReader(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type);
        this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
    }

    public OdsReader<T> sheet(int index) { this.sheetIndex = index; return this; }
    public OdsReader<T> sheet(String name) { this.sheetName = name; return this; }
    public OdsReader<T> headerRow(int row) { this.headerRow = row; return this; }

    public List<T> read(Path path) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(path.toFile())) {
            return readDocument(doc);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (SheetzException e) {
            throw e;
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS file: " + path, e);
        }
    }

    public List<T> read(InputStream input) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(input)) {
            return readDocument(doc);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (SheetzException e) {
            throw e;
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS from stream", e);
        }
    }

    public ValidationResult<T> validate(Path path) {
        long start = System.currentTimeMillis();
        List<T> valid = new ArrayList<>();
        List<ValidationResult.RowError> errors = new ArrayList<>();
        int total = 0;
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(path.toFile())) {
            OdfTable table = resolveTable(doc);
            int hdrRow = headerRow >= 0 ? headerRow : config.headerRow();
            List<String> headers = readHeaders(table, hdrRow);
            ColumnResolver resolver = new ColumnResolver(headers);
            List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
            int lastRow = getLastDataRow(table);
            for (int rowNum = hdrRow + 1; rowNum <= lastRow; rowNum++) {
                total++;
                OdfTableRow row = table.getRowByIndex(rowNum);
                if (row == null || isRowEmpty(table, rowNum, headers.size())) {
                    if (config.skipEmptyRows()) continue;
                }
                try {
                    T obj = mapRow(table, rowNum, fields, headers);
                    if (obj != null) valid.add(obj);
                } catch (MappingException e) {
                    errors.add(new ValidationResult.RowError(e.row(), e.column(), e.getMessage(), e.value(), e.getCause()));
                } catch (Exception e) {
                    errors.add(new ValidationResult.RowError(rowNum + 1, e.getMessage()));
                }
            }
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (Exception e) {
            errors.add(new ValidationResult.RowError(-1, "Failed to read file: " + e.getMessage()));
        }
        return new ValidationResult<>(valid, errors, total, System.currentTimeMillis() - start);
    }

    public static List<Map<String, Object>> readMaps(Path path, SheetzConfig config) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(path.toFile())) {
            return readMapsFromDoc(doc, config);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS file", e);
        }
    }

    public static List<Map<String, Object>> readMaps(InputStream input, SheetzConfig config) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(input)) {
            return readMapsFromDoc(doc, config);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS from stream", e);
        }
    }

    public static List<String[]> readRaw(Path path, SheetzConfig config) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(path.toFile())) {
            return readRawFromDoc(doc, config);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS file", e);
        }
    }

    public static List<String[]> readRaw(InputStream input, SheetzConfig config) {
        try (OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(input)) {
            return readRawFromDoc(doc, config);
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (Exception e) {
            throw new SheetzException("Failed to read ODS from stream", e);
        }
    }

    // ---- Internal ----

    private List<T> readDocument(OdfSpreadsheetDocument doc) {
        OdfTable table = resolveTable(doc);
        int hdrRow = headerRow >= 0 ? headerRow : config.headerRow();
        List<String> headers = readHeaders(table, hdrRow);
        ColumnResolver resolver = new ColumnResolver(headers);
        List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
        List<T> results = new ArrayList<>();
        int lastRow = getLastDataRow(table);
        for (int rowNum = hdrRow + 1; rowNum <= lastRow; rowNum++) {
            if (isRowEmpty(table, rowNum, headers.size()) && config.skipEmptyRows()) continue;
            T obj = mapRow(table, rowNum, fields, headers);
            if (obj != null) results.add(obj);
        }
        return results;
    }

    private OdfTable resolveTable(OdfSpreadsheetDocument doc) {
        List<OdfTable> tables = doc.getTableList(false);
        if (tables.isEmpty()) throw new SheetzException("No sheets found in ODS document");
        if (sheetName != null) {
            OdfTable table = doc.getTableByName(sheetName);
            if (table == null) throw new SheetzException("Sheet not found: " + sheetName);
            return table;
        }
        if (sheetIndex >= tables.size()) {
            throw new SheetzException("Sheet index " + sheetIndex + " out of range (0-" + (tables.size() - 1) + ")");
        }
        return tables.get(sheetIndex);
    }

    private List<String> readHeaders(OdfTable table, int hdrRow) {
        List<String> headers = new ArrayList<>();
        int colCount = table.getColumnCount();
        for (int c = 0; c < colCount; c++) {
            OdfTableCell cell = table.getCellByPosition(c, hdrRow);
            String val = cell != null ? getCellAsString(cell) : "";
            if (val == null) val = "";
            headers.add(val.trim());
        }
        // Trim trailing empty headers
        while (!headers.isEmpty() && headers.get(headers.size() - 1).isEmpty()) {
            headers.remove(headers.size() - 1);
        }
        return headers;
    }

    private T mapRow(OdfTable table, int rowNum, List<RowMapper.ResolvedField> fields, List<String> headers) {
        return RowMapper.mapObjectRow(mapping, fields,
            colIdx -> getCellValue(table, rowNum, colIdx),
            rowNum + 1, headers, config);
    }

    private boolean isRowEmpty(OdfTable table, int rowNum, int colCount) {
        for (int c = 0; c < colCount; c++) {
            OdfTableCell cell = table.getCellByPosition(c, rowNum);
            if (cell != null) {
                String val = getCellAsString(cell);
                if (val != null && !val.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    private int getLastDataRow(OdfTable table) {
        int rowCount = table.getRowCount();
        // ODS may report many extra empty rows; trim from the end
        while (rowCount > 0 && isRowEmpty(table, rowCount - 1, table.getColumnCount())) {
            rowCount--;
        }
        return rowCount - 1;
    }

    static Object getCellValue(OdfTable table, int row, int col) {
        OdfTableCell cell = table.getCellByPosition(col, row);
        if (cell == null) return null;
        String type = cell.getValueType();
        if (type == null) return null;
        switch (type) {
            case "float":
            case "currency":
            case "percentage":
                return cell.getDoubleValue();
            case "boolean":
                return cell.getBooleanValue();
            case "date":
                // Return as string and let converters handle parsing
                return cell.getDisplayText();
            case "time":
                return cell.getDisplayText();
            case "string":
            default:
                return cell.getStringValue();
        }
    }

    static String getCellAsString(OdfTableCell cell) {
        if (cell == null) return null;
        String type = cell.getValueType();
        if (type == null) {
            String text = cell.getDisplayText();
            return text != null && !text.isEmpty() ? text : null;
        }
        switch (type) {
            case "float":
            case "currency":
            case "percentage":
                Double d = cell.getDoubleValue();
                if (d == null) return null;
                if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d.doubleValue());
                return d.toString();
            case "boolean":
                Boolean b = cell.getBooleanValue();
                return b != null ? b.toString() : null;
            default:
                String s = cell.getStringValue();
                return s != null ? s : cell.getDisplayText();
        }
    }

    private static List<Map<String, Object>> readMapsFromDoc(OdfSpreadsheetDocument doc, SheetzConfig config) {
        List<OdfTable> tables = doc.getTableList(false);
        if (tables.isEmpty()) return Collections.emptyList();
        OdfTable table = tables.get(0);
        int hdrRow = config.headerRow();
        List<String> headers = new ArrayList<>();
        int colCount = table.getColumnCount();
        for (int c = 0; c < colCount; c++) {
            OdfTableCell cell = table.getCellByPosition(c, hdrRow);
            String val = cell != null ? getCellAsString(cell) : "";
            headers.add(val != null ? val.trim() : "");
        }
        while (!headers.isEmpty() && headers.get(headers.size() - 1).isEmpty()) {
            headers.remove(headers.size() - 1);
        }
        if (headers.isEmpty()) return Collections.emptyList();

        List<Map<String, Object>> results = new ArrayList<>();
        int lastRow = table.getRowCount();
        for (int rowNum = hdrRow + 1; rowNum < lastRow; rowNum++) {
            boolean empty = true;
            Map<String, Object> map = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                String h = headers.get(c);
                if (h != null && !h.isEmpty()) {
                    Object val = getCellValue(table, rowNum, c);
                    map.put(h, val);
                    if (val != null) empty = false;
                }
            }
            if (empty && config.skipEmptyRows()) continue;
            results.add(map);
        }
        return results;
    }

    private static List<String[]> readRawFromDoc(OdfSpreadsheetDocument doc, SheetzConfig config) {
        List<OdfTable> tables = doc.getTableList(false);
        if (tables.isEmpty()) return Collections.emptyList();
        OdfTable table = tables.get(0);
        int colCount = table.getColumnCount();
        List<String[]> results = new ArrayList<>();
        int lastRow = table.getRowCount();
        for (int rowNum = 0; rowNum < lastRow; rowNum++) {
            boolean empty = true;
            String[] arr = new String[colCount];
            for (int c = 0; c < colCount; c++) {
                OdfTableCell cell = table.getCellByPosition(c, rowNum);
                String val = getCellAsString(cell);
                arr[c] = val;
                if (val != null && !val.isEmpty()) empty = false;
            }
            if (empty && rowNum > 0) continue; // Skip empty data rows but keep header
            results.add(arr);
        }
        return results;
    }

    private static SheetzException odsNotAvailable() {
        return new SheetzException(
            "ODS format support requires the ODF Toolkit library. " +
            "Add the following dependency to your project:\n" +
            "  <dependency>\n" +
            "    <groupId>org.odftoolkit</groupId>\n" +
            "    <artifactId>odfdom-java</artifactId>\n" +
            "    <version>0.12.0</version>\n" +
            "  </dependency>");
    }
}
