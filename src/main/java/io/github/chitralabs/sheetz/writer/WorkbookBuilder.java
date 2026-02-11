package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.exception.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Builder for creating multi-sheet Excel workbooks.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Sheetz.workbook()
 *     .sheet("Products", products)
 *     .sheet("Employees", employees)
 *     .write("report.xlsx");
 * }</pre>
 */
public final class WorkbookBuilder {
    private final SheetzConfig config;
    private final List<SheetData<?>> sheets = new ArrayList<>();

    public WorkbookBuilder(SheetzConfig config) { this.config = Objects.requireNonNull(config); }

    @SuppressWarnings("unchecked")
    public <T> WorkbookBuilder sheet(String name, List<T> data) {
        if (data == null || data.isEmpty()) throw new SheetzException("Sheet data cannot be empty");
        Class<T> type = (Class<T>) data.get(0).getClass();
        sheets.add(new SheetData<>(name, data, type));
        return this;
    }

    public void write(String path) { write(Path.of(path)); }

    public void write(Path path) {
        if (sheets.isEmpty()) throw new SheetzException("No sheets to write");
        Format format = Format.detect(path.toString());
        if (format == Format.CSV) throw new SheetzException("CSV does not support multiple sheets");
        try (Workbook wb = format == Format.XLSX ? new XSSFWorkbook() : new HSSFWorkbook();
             OutputStream os = Files.newOutputStream(path)) {
            CellStyle headerStyle = ExcelWriteSupport.createHeaderStyle(wb);
            CellStyle dateStyle = ExcelWriteSupport.createDateStyle(wb, config);
            for (SheetData<?> sd : sheets) writeSheet(wb, sd, headerStyle, dateStyle);
            wb.write(os);
        } catch (IOException e) { throw new SheetzException("Failed to write workbook", e); }
    }

    private <T> void writeSheet(Workbook wb, SheetData<T> sd, CellStyle headerStyle, CellStyle dateStyle) {
        Sheet sheet = wb.createSheet(sd.name);
        MappingCache.ClassMapping mapping = MappingCache.get(sd.type);
        List<FieldMapping> fields = mapping.fields();
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields.get(i).headerName());
            cell.setCellStyle(headerStyle);
        }
        int rowNum = 1;
        for (T obj : sd.data) {
            Row row = sheet.createRow(rowNum++);
            ExcelWriteSupport.writeRow(obj, row, fields, dateStyle);
        }
    }

    private static final class SheetData<T> {
        final String name;
        final List<T> data;
        final Class<T> type;
        SheetData(String name, List<T> data, Class<T> type) {
            this.name = name;
            this.data = data;
            this.type = type;
        }
    }
}
