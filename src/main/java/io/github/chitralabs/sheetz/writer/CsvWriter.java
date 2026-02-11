package io.github.chitralabs.sheetz.writer;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;
import com.opencsv.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Writer for CSV files with annotation-based mapping.
 *
 * @param <T> the type of objects to write
 */
public final class CsvWriter<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private char delimiter = ',';

    public CsvWriter(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type); this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
    }

    public CsvWriter<T> delimiter(char d) { this.delimiter = d; return this; }

    public void write(List<T> data, Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) { write(data, writer); }
        catch (IOException e) { throw new SheetzException("Failed to write file: " + path, e); }
    }

    public void write(List<T> data, OutputStream os) { write(data, new OutputStreamWriter(os)); }

    public void write(List<T> data, Writer writer) {
        if (data == null || data.isEmpty()) throw new SheetzException("Data cannot be null or empty");
        List<FieldMapping> fields = mapping.fields();
        try (CSVWriter csv = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            String[] headers = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++) headers[i] = fields.get(i).headerName();
            csv.writeNext(headers);
            for (T obj : data) csv.writeNext(toStringArray(obj, fields));
        } catch (IOException e) { throw new SheetzException("Failed to write CSV", e); }
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(T obj, List<FieldMapping> fields) {
        String[] arr = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            FieldMapping fm = fields.get(i);
            try {
                Object value = fm.getValue(obj);
                if (value == null) { arr[i] = ""; continue; }
                if (fm.hasCustomConverter()) {
                    Object conv = ((Converter<Object>) fm.converter()).toCell(value);
                    arr[i] = conv != null ? conv.toString() : "";
                } else {
                    Converter<?> conv = Converters.get(value.getClass());
                    if (conv != null) {
                        @SuppressWarnings("rawtypes")
                        Object c = ((Converter) conv).toCell(value);
                        arr[i] = c != null ? c.toString() : "";
                    } else {
                        arr[i] = value.toString();
                    }
                }
            } catch (IllegalAccessException e) { arr[i] = ""; }
        }
        return arr;
    }
}
