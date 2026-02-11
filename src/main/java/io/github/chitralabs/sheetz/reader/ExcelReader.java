package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reader for Excel files (XLSX, XLS) with annotation-based mapping.
 *
 * <p>Uses Apache POI for workbook parsing with support for sheet selection,
 * header row configuration, and type-safe object mapping.</p>
 *
 * @param <T> the type to map rows to
 */
public final class ExcelReader<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private int sheetIndex = 0;
    private String sheetName;
    private int headerRow = -1;

    public ExcelReader(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type); this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
    }

    public ExcelReader<T> sheet(int index) { this.sheetIndex = index; return this; }
    public ExcelReader<T> sheet(String name) { this.sheetName = name; return this; }
    public ExcelReader<T> headerRow(int row) { this.headerRow = row; return this; }

    public List<T> read(Path path) {
        Format format = Format.detect(path.toString());
        try (InputStream is = Files.newInputStream(path)) { return read(is, format); }
        catch (IOException e) { throw new SheetzException("Failed to read file: " + path, e); }
    }

    public List<T> read(InputStream input, Format format) {
        Objects.requireNonNull(input); if (!format.isExcel()) throw new SheetzException("Not an Excel format: " + format);
        try (Workbook wb = format == Format.XLSX ? new XSSFWorkbook(input) : new HSSFWorkbook(input)) {
            Sheet sheet = sheetName != null ? wb.getSheet(sheetName) : wb.getSheetAt(sheetIndex);
            if (sheet == null) throw new SheetzException("Sheet not found");
            return readSheet(sheet);
        } catch (IOException e) { throw new SheetzException("Failed to read Excel", e); }
    }

    public ValidationResult<T> validate(Path path) {
        long start = System.currentTimeMillis();
        List<T> valid = new ArrayList<>(); List<ValidationResult.RowError> errors = new ArrayList<>(); int total = 0;
        try (InputStream is = Files.newInputStream(path);
             Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            int hdrRow = headerRow >= 0 ? headerRow : config.headerRow();
            List<String> headers = readHeaders(sheet.getRow(hdrRow));
            ColumnResolver resolver = new ColumnResolver(headers);
            List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
            for (int rowNum = hdrRow + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                total++;
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowEmpty(row)) { if (config.skipEmptyRows()) continue; }
                try {
                    T obj = mapRow(row, fields, rowNum + 1, headers);
                    if (obj != null) valid.add(obj);
                }
                catch (MappingException e) { errors.add(new ValidationResult.RowError(e.row(), e.column(), e.getMessage(), e.value(), e.getCause())); }
                catch (Exception e) { errors.add(new ValidationResult.RowError(rowNum + 1, e.getMessage())); }
            }
        } catch (IOException e) { errors.add(new ValidationResult.RowError(-1, "Failed to read file: " + e.getMessage())); }
        return new ValidationResult<>(valid, errors, total, System.currentTimeMillis() - start);
    }

    public static List<Map<String, Object>> readMaps(Path path, SheetzConfig config) {
        try (InputStream is = Files.newInputStream(path)) { return readMaps(is, config); }
        catch (IOException e) { throw new SheetzException("Failed to read file", e); }
    }

    public static List<Map<String, Object>> readMaps(InputStream input, SheetzConfig config) {
        try (Workbook wb = WorkbookFactory.create(input)) {
            Sheet sheet = wb.getSheetAt(0);
            List<String> headers = new ArrayList<>();
            Row hdrRow = sheet.getRow(config.headerRow());
            if (hdrRow == null) return Collections.emptyList();
            for (int i = 0; i < hdrRow.getLastCellNum(); i++) headers.add(getCellAsString(hdrRow.getCell(i)));
            List<Map<String, Object>> results = new ArrayList<>();
            for (int rowNum = config.headerRow() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum); if (row == null) continue;
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String h = headers.get(i); if (h != null && !h.isEmpty()) map.put(h, getCellValue(row.getCell(i)));
                }
                results.add(map);
            }
            return results;
        } catch (IOException e) { throw new SheetzException("Failed to read file", e); }
    }

    public static List<String[]> readRaw(Path path, SheetzConfig config) {
        try (InputStream is = Files.newInputStream(path)) { return readRaw(is, config); }
        catch (IOException e) { throw new SheetzException("Failed to read file", e); }
    }

    public static List<String[]> readRaw(InputStream input, SheetzConfig config) {
        try (Workbook wb = WorkbookFactory.create(input)) {
            Sheet sheet = wb.getSheetAt(0);
            List<String[]> results = new ArrayList<>();
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) { results.add(new String[0]); continue; }
                String[] arr = new String[row.getLastCellNum()];
                for (int i = 0; i < row.getLastCellNum(); i++) arr[i] = getCellAsString(row.getCell(i));
                results.add(arr);
            }
            return results;
        } catch (IOException e) { throw new SheetzException("Failed to read file", e); }
    }

    private List<T> readSheet(Sheet sheet) {
        int hdrRow = headerRow >= 0 ? headerRow : config.headerRow();
        Row headerRowObj = sheet.getRow(hdrRow);
        if (headerRowObj == null) throw new SheetzException("Header row is empty");
        List<String> headers = readHeaders(headerRowObj);
        ColumnResolver resolver = new ColumnResolver(headers);
        List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
        List<T> results = new ArrayList<>();
        for (int rowNum = hdrRow + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isRowEmpty(row)) { if (config.skipEmptyRows()) continue; }
            T obj = mapRow(row, fields, rowNum + 1, headers);
            if (obj != null) results.add(obj);
        }
        return results;
    }

    private List<String> readHeaders(Row row) {
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String val = getCellAsString(row.getCell(i));
            headers.add(val != null ? val.trim() : "");
        }
        return headers;
    }

    private T mapRow(Row row, List<RowMapper.ResolvedField> fields, int rowNum, List<String> headers) {
        return RowMapper.mapObjectRow(mapping, fields,
            colIdx -> getCellValue(row != null ? row.getCell(colIdx) : null),
            rowNum, headers, config);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellAsString(cell);
                if (val != null && !val.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    static Object getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
            case BOOLEAN: return cell.getBooleanCellValue();
            case FORMULA: try { return cell.getNumericCellValue(); } catch (Exception e) { return cell.getStringCellValue(); }
            case BLANK: return null;
            default: return cell.toString();
        }
    }

    static String getCellAsString(Cell cell) {
        Object val = getCellValue(cell);
        if (val == null) return null;
        if (val instanceof Double) { Double d = (Double) val; if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d.doubleValue()); }
        return val.toString();
    }
}
