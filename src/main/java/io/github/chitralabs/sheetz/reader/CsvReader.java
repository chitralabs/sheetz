package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reader for CSV files with annotation-based mapping.
 *
 * <p>Uses OpenCSV for correct RFC 4180 parsing, including support for
 * embedded newlines, escaped quotes, and configurable delimiters.</p>
 *
 * @param <T> the type to map rows to
 */
public final class CsvReader<T> {
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private char delimiter = ',';

    public CsvReader(Class<T> type, SheetzConfig config) {
        this.type = Objects.requireNonNull(type); this.config = Objects.requireNonNull(config);
        this.mapping = MappingCache.get(type);
    }

    public CsvReader<T> delimiter(char d) { this.delimiter = d; return this; }

    public List<T> read(Path path) {
        try (Reader reader = Files.newBufferedReader(path, config.charset())) { return read(reader); }
        catch (IOException e) { throw new SheetzException("Failed to read file: " + path, e); }
    }

    public List<T> read(InputStream is) { return read(new InputStreamReader(is)); }

    public List<T> read(Reader reader) {
        try (CSVReader csv = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build()) {
            String[] headerArr = csv.readNext();
            if (headerArr == null) return new ArrayList<>();
            List<String> headers = Arrays.asList(headerArr);
            ColumnResolver resolver = new ColumnResolver(headers);
            List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
            List<T> results = new ArrayList<>();
            String[] line; int rowNum = 1;
            while ((line = csv.readNext()) != null) {
                if (config.skipEmptyRows() && isEmpty(line)) { rowNum++; continue; }
                T obj = RowMapper.mapStringArray(mapping, fields, line, rowNum + 1, headers, config);
                if (obj != null) results.add(obj);
                rowNum++;
            }
            return results;
        } catch (MappingException e) { throw e; }
        catch (Exception e) { throw new SheetzException("Failed to read CSV", e); }
    }

    public ValidationResult<T> validate(Path path) {
        long start = System.currentTimeMillis();
        List<T> valid = new ArrayList<>(); List<ValidationResult.RowError> errors = new ArrayList<>(); int total = 0;
        try (Reader reader = Files.newBufferedReader(path, config.charset());
             CSVReader csv = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build()) {
            String[] headerArr = csv.readNext();
            if (headerArr == null) return new ValidationResult<>(valid, errors, 0, System.currentTimeMillis() - start);
            List<String> headers = Arrays.asList(headerArr);
            ColumnResolver resolver = new ColumnResolver(headers);
            List<RowMapper.ResolvedField> fields = RowMapper.resolveFields(mapping, resolver);
            String[] line; int rowNum = 1;
            while ((line = csv.readNext()) != null) {
                total++;
                if (config.skipEmptyRows() && isEmpty(line)) { rowNum++; continue; }
                try {
                    T obj = RowMapper.mapStringArray(mapping, fields, line, rowNum + 1, headers, config);
                    if (obj != null) valid.add(obj);
                }
                catch (MappingException e) { errors.add(new ValidationResult.RowError(e.row(), e.column(), e.getMessage(), e.value(), e.getCause())); }
                catch (Exception e) { errors.add(new ValidationResult.RowError(rowNum + 1, e.getMessage())); }
                rowNum++;
            }
        } catch (IOException | CsvValidationException e) { errors.add(new ValidationResult.RowError(-1, "Failed to read file: " + e.getMessage())); }
        return new ValidationResult<>(valid, errors, total, System.currentTimeMillis() - start);
    }

    public static List<Map<String, Object>> readMaps(Path path, SheetzConfig config) {
        try (Reader reader = Files.newBufferedReader(path, config.charset()); CSVReader csv = new CSVReader(reader)) {
            String[] headers = csv.readNext();
            if (headers == null) return new ArrayList<>();
            List<Map<String, Object>> results = new ArrayList<>();
            String[] line;
            while ((line = csv.readNext()) != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < line.length; i++) map.put(headers[i], line[i]);
                results.add(map);
            }
            return results;
        } catch (Exception e) { throw new SheetzException("Failed to read CSV", e); }
    }

    public static List<Map<String, Object>> readMaps(InputStream is, SheetzConfig config) {
        try (CSVReader csv = new CSVReader(new InputStreamReader(is))) {
            String[] headers = csv.readNext();
            if (headers == null) return new ArrayList<>();
            List<Map<String, Object>> results = new ArrayList<>();
            String[] line;
            while ((line = csv.readNext()) != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < line.length; i++) map.put(headers[i], line[i]);
                results.add(map);
            }
            return results;
        } catch (Exception e) { throw new SheetzException("Failed to read CSV", e); }
    }

    public static List<String[]> readRaw(Path path, SheetzConfig config) {
        try (Reader reader = Files.newBufferedReader(path, config.charset()); CSVReader csv = new CSVReader(reader)) { return csv.readAll(); }
        catch (Exception e) { throw new SheetzException("Failed to read CSV", e); }
    }

    public static List<String[]> readRaw(InputStream is, SheetzConfig config) {
        try (CSVReader csv = new CSVReader(new InputStreamReader(is))) { return csv.readAll(); }
        catch (Exception e) { throw new SheetzException("Failed to read CSV", e); }
    }

    private boolean isEmpty(String[] line) {
        if (line == null || line.length == 0) return true;
        for (String val : line) if (val != null && !val.trim().isEmpty()) return false;
        return true;
    }
}
