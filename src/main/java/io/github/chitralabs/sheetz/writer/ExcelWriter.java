package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.exception.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Writer for Excel files (XLSX, XLS) with annotation-based mapping.
 *
 * @param <T> the type of objects to write
 */
public final class ExcelWriter<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private String sheetName;
    private boolean autoSize = false;
    private boolean freezeHeader = false;
    private boolean streaming = false;
    private int streamingWindowSize = 100;

    public ExcelWriter(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type); this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
        this.sheetName = config.defaultSheetName();
    }

    public ExcelWriter<T> sheetName(String name) { this.sheetName = name; return this; }
    public ExcelWriter<T> autoSize(boolean v) { this.autoSize = v; return this; }
    public ExcelWriter<T> freezeHeader(boolean v) { this.freezeHeader = v; return this; }
    public ExcelWriter<T> streaming(boolean v) { this.streaming = v; return this; }
    /** Sets the SXSSFWorkbook row window size for streaming mode (default: 100). */
    public ExcelWriter<T> streamingWindowSize(int size) {
        if (size < 1) throw new IllegalArgumentException("Streaming window size must be >= 1");
        this.streamingWindowSize = size; return this;
    }

    public void write(List<T> data, Path path, Format format) {
        try (OutputStream os = Files.newOutputStream(path)) { write(data, os, format); }
        catch (IOException e) { throw new SheetzException("Failed to write file: " + path, e); }
    }

    public void write(List<T> data, OutputStream os, Format format) {
        if (data == null || data.isEmpty()) throw new SheetzException("Data cannot be null or empty");
        boolean useStreaming = streaming || (data.size() > config.streamingThreshold() && format == Format.XLSX);
        try (Workbook wb = createWorkbook(format, useStreaming)) {
            Sheet sheet = wb.createSheet(sheetName);
            CellStyle headerStyle = ExcelWriteSupport.createHeaderStyle(wb);
            CellStyle dateStyle = ExcelWriteSupport.createDateStyle(wb, config);
            List<FieldMapping> fields = mapping.fields();
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields.get(i).headerName());
                cell.setCellStyle(headerStyle);
            }
            if (freezeHeader) sheet.createFreezePane(0, 1);
            int rowNum = 1;
            for (T obj : data) { Row row = sheet.createRow(rowNum++); ExcelWriteSupport.writeRow(obj, row, fields, dateStyle); }
            if (autoSize && !useStreaming) for (int i = 0; i < fields.size(); i++) sheet.autoSizeColumn(i);
            for (int i = 0; i < fields.size(); i++) { int width = fields.get(i).width(); if (width > 0) sheet.setColumnWidth(i, width * 256); }
            wb.write(os);
            if (wb instanceof SXSSFWorkbook) ((SXSSFWorkbook) wb).dispose();
        } catch (IOException e) { throw new SheetzException("Failed to write Excel", e); }
    }

    private Workbook createWorkbook(Format format, boolean streaming) {
        if (format == Format.XLSX) return streaming ? new SXSSFWorkbook(streamingWindowSize) : new XSSFWorkbook();
        if (format == Format.XLS) return new HSSFWorkbook();
        throw new SheetzException("Not an Excel format: " + format);
    }
}
