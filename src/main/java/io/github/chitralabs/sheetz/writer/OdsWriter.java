package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.exception.*;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Writer for ODS (OpenDocument Spreadsheet) files.
 *
 * @param <T> the type of objects to write
 */
public final class OdsWriter<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private String sheetName;

    public OdsWriter(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type);
        this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
        this.sheetName = config.defaultSheetName();
    }

    public OdsWriter<T> sheetName(String name) { this.sheetName = name; return this; }

    public void write(List<T> data, Path path) {
        try (OutputStream os = Files.newOutputStream(path)) {
            write(data, os);
        } catch (IOException e) {
            throw new SheetzException("Failed to write ODS file: " + path, e);
        }
    }

    public void write(List<T> data, OutputStream os) {
        if (data == null || data.isEmpty()) throw new SheetzException("Data cannot be null or empty");
        try {
            OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.newSpreadsheetDocument();
            // Remove the default empty table
            List<OdfTable> existing = doc.getTableList(false);
            for (OdfTable t : existing) t.remove();

            List<FieldMapping> fields = mapping.fields();
            OdfTable table = OdfTable.newTable(doc, data.size() + 1, fields.size());
            table.setTableName(sheetName);

            // Write header row
            for (int i = 0; i < fields.size(); i++) {
                table.getCellByPosition(i, 0).setStringValue(fields.get(i).headerName());
            }

            // Write data rows
            int rowNum = 1;
            for (T obj : data) {
                OdsWriteSupport.writeRow(obj, table, rowNum++, fields);
            }

            doc.save(os);
            doc.close();
        } catch (NoClassDefFoundError e) {
            throw odsNotAvailable();
        } catch (SheetzException e) {
            throw e;
        } catch (Exception e) {
            throw new SheetzException("Failed to write ODS", e);
        }
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
