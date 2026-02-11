package io.github.chitralabs.sheetz;

import io.github.chitralabs.sheetz.cache.MappingCache;
import io.github.chitralabs.sheetz.convert.Converter;
import io.github.chitralabs.sheetz.convert.Converters;
import io.github.chitralabs.sheetz.exception.SheetzException;
import io.github.chitralabs.sheetz.reader.*;
import io.github.chitralabs.sheetz.writer.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main entry point for the Sheetz library.
 *
 * <p>Provides static methods for reading and writing Excel (XLSX, XLS) and CSV files
 * with automatic type conversion and annotation-based mapping.</p>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // Read Excel file to POJOs
 * List<Product> products = Sheetz.read("products.xlsx", Product.class);
 *
 * // Write POJOs to Excel
 * Sheetz.write(products, "output.xlsx");
 *
 * // Stream large files with constant memory
 * Sheetz.stream("huge.xlsx", Product.class)
 *     .forEach(product -> process(product));
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods in this class are thread-safe. Global configuration is stored
 * in an {@link AtomicReference} and can be safely modified from any thread.</p>
 *
 * @see SheetzConfig
 * @see io.github.chitralabs.sheetz.annotation.Column
 * @see StreamingReader
 */
public final class Sheetz {
    private static final AtomicReference<SheetzConfig> CONFIG = new AtomicReference<>(SheetzConfig.defaults());
    private Sheetz() {}

    // ==================== READ OPERATIONS ====================

    /**
     * Reads an Excel or CSV file and maps rows to objects of the specified type.
     *
     * @param <T> the type to map rows to
     * @param path the file path (supports .xlsx, .xls, .csv)
     * @param type the class to map rows to
     * @return list of mapped objects
     * @throws SheetzException if the file cannot be read or mapping fails
     */
    public static <T> List<T> read(String path, Class<T> type) { return read(Path.of(path), type); }

    /**
     * Reads an Excel or CSV file and maps rows to objects of the specified type.
     *
     * @param <T> the type to map rows to
     * @param path the file path
     * @param type the class to map rows to
     * @return list of mapped objects
     * @throws SheetzException if the file cannot be read or mapping fails
     */
    public static <T> List<T> read(Path path, Class<T> type) {
        validateReadPath(path);
        Format fmt = Format.detect(path.toString());
        return fmt.isCsv() ? new CsvReader<>(type, CONFIG.get()).read(path) : new ExcelReader<>(type, CONFIG.get()).read(path);
    }

    /**
     * Reads from an input stream and maps rows to objects.
     *
     * @param <T> the type to map rows to
     * @param input the input stream
     * @param type the class to map rows to
     * @param format the file format
     * @return list of mapped objects
     * @throws SheetzException if reading or mapping fails
     */
    public static <T> List<T> read(InputStream input, Class<T> type, Format format) {
        Objects.requireNonNull(input); Objects.requireNonNull(format);
        return format.isCsv() ? new CsvReader<>(type, CONFIG.get()).read(input) : new ExcelReader<>(type, CONFIG.get()).read(input, format);
    }

    /**
     * Reads a file as a list of maps (header -&gt; value).
     * Useful when you don't have a model class.
     *
     * @param path the file path
     * @return list of maps where keys are column headers
     */
    public static List<Map<String, Object>> readMaps(String path) { return readMaps(Path.of(path)); }

    /**
     * Reads a file as a list of maps (header -&gt; value).
     *
     * @param path the file path
     * @return list of maps where keys are column headers
     */
    public static List<Map<String, Object>> readMaps(Path path) {
        validateReadPath(path);
        Format fmt = Format.detect(path.toString());
        return fmt.isCsv() ? CsvReader.readMaps(path, CONFIG.get()) : ExcelReader.readMaps(path, CONFIG.get());
    }

    /**
     * Reads from an input stream as a list of maps (header -&gt; value).
     *
     * @param input the input stream
     * @param format the file format
     * @return list of maps where keys are column headers
     */
    public static List<Map<String, Object>> readMaps(InputStream input, Format format) {
        Objects.requireNonNull(input); Objects.requireNonNull(format);
        return format.isCsv() ? CsvReader.readMaps(input, CONFIG.get()) : ExcelReader.readMaps(input, CONFIG.get());
    }

    /**
     * Reads a file as raw string arrays (no type conversion).
     *
     * @param path the file path
     * @return list of string arrays, one per row (including header)
     */
    public static List<String[]> readRaw(String path) { return readRaw(Path.of(path)); }

    /**
     * Reads a file as raw string arrays (no type conversion).
     *
     * @param path the file path
     * @return list of string arrays, one per row (including header)
     */
    public static List<String[]> readRaw(Path path) {
        validateReadPath(path);
        Format fmt = Format.detect(path.toString());
        return fmt.isCsv() ? CsvReader.readRaw(path, CONFIG.get()) : ExcelReader.readRaw(path, CONFIG.get());
    }

    /**
     * Reads from an input stream as raw string arrays (no type conversion).
     *
     * @param input the input stream
     * @param format the file format
     * @return list of string arrays, one per row (including header)
     */
    public static List<String[]> readRaw(InputStream input, Format format) {
        Objects.requireNonNull(input); Objects.requireNonNull(format);
        return format.isCsv() ? CsvReader.readRaw(input, CONFIG.get()) : ExcelReader.readRaw(input, CONFIG.get());
    }

    /**
     * Creates a reader builder for fine-grained control over reading.
     *
     * @param <T> the type to map rows to
     * @param type the class to map rows to
     * @return a new reader builder
     */
    public static <T> ReaderBuilder<T> reader(Class<T> type) { return new ReaderBuilder<>(type, CONFIG.get()); }

    /**
     * Reads only the first {@code n} rows from a file using streaming.
     * Memory-efficient for large files when you only need a preview.
     *
     * @param <T> the type to map rows to
     * @param path the file path
     * @param type the class to map rows to
     * @param n the maximum number of rows to read
     * @return list of mapped objects (up to n rows)
     * @throws SheetzException if the file cannot be read
     */
    public static <T> List<T> readFirst(String path, Class<T> type, int n) { return readFirst(Path.of(path), type, n); }

    /**
     * Reads only the first {@code n} rows from a file using streaming.
     * Memory-efficient for large files when you only need a preview.
     *
     * @param <T> the type to map rows to
     * @param path the file path
     * @param type the class to map rows to
     * @param n the maximum number of rows to read
     * @return list of mapped objects (up to n rows)
     * @throws SheetzException if the file cannot be read
     */
    public static <T> List<T> readFirst(Path path, Class<T> type, int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        if (n == 0) return Collections.emptyList();
        validateReadPath(path);
        List<T> result = new ArrayList<>(n);
        try (StreamingReader<T> reader = stream(path, type)) {
            for (T item : reader) {
                result.add(item);
                if (result.size() >= n) break;
            }
        }
        return result;
    }

    // ==================== WRITE OPERATIONS ====================

    /**
     * Writes a list of objects to an Excel or CSV file.
     * Format is determined by file extension.
     *
     * @param <T> the type of objects
     * @param data the data to write
     * @param path the output file path
     * @throws SheetzException if data is empty or writing fails
     */
    public static <T> void write(List<T> data, String path) { write(data, Path.of(path)); }

    /**
     * Writes a list of objects to an Excel or CSV file.
     *
     * @param <T> the type of objects
     * @param data the data to write
     * @param path the output file path
     * @throws SheetzException if data is empty or writing fails
     */
    @SuppressWarnings("unchecked")
    public static <T> void write(List<T> data, Path path) {
        if (data == null || data.isEmpty()) throw new SheetzException("Data cannot be null or empty");
        Format fmt = Format.detect(path.toString());
        Class<T> type = (Class<T>) data.get(0).getClass();
        if (fmt.isCsv()) new CsvWriter<>(type, CONFIG.get()).write(data, path);
        else new ExcelWriter<>(type, CONFIG.get()).write(data, path, fmt);
    }

    /**
     * Writes a list of objects to an output stream.
     *
     * @param <T> the type of objects
     * @param data the data to write
     * @param output the output stream
     * @param format the file format
     * @throws SheetzException if data is empty or writing fails
     */
    @SuppressWarnings("unchecked")
    public static <T> void write(List<T> data, OutputStream output, Format format) {
        if (data == null || data.isEmpty()) throw new SheetzException("Data cannot be null or empty");
        Class<T> type = (Class<T>) data.get(0).getClass();
        if (format.isCsv()) new CsvWriter<>(type, CONFIG.get()).write(data, output);
        else new ExcelWriter<>(type, CONFIG.get()).write(data, output, format);
    }

    /**
     * Creates a writer builder for fine-grained control over writing.
     *
     * @param <T> the type of objects
     * @param type the class of objects to write
     * @return a new writer builder
     */
    public static <T> WriterBuilder<T> writer(Class<T> type) { return new WriterBuilder<>(type, CONFIG.get()); }

    /**
     * Creates a workbook builder for multi-sheet Excel files.
     *
     * @return a new workbook builder
     */
    public static WorkbookBuilder workbook() { return new WorkbookBuilder(CONFIG.get()); }

    // ==================== STREAMING OPERATIONS ====================

    /**
     * Creates a streaming reader for memory-efficient processing of large files.
     *
     * <p>Uses SAX-based parsing for XLSX files, keeping memory usage constant
     * regardless of file size (~10MB for any file).</p>
     *
     * @param <T> the type to map rows to
     * @param path the file path
     * @param type the class to map rows to
     * @return a streaming reader
     */
    public static <T> StreamingReader<T> stream(String path, Class<T> type) { return stream(Path.of(path), type); }

    /**
     * Creates a streaming reader for memory-efficient processing of large files.
     *
     * @param <T> the type to map rows to
     * @param path the file path
     * @param type the class to map rows to
     * @return a streaming reader
     */
    public static <T> StreamingReader<T> stream(Path path, Class<T> type) { return new StreamingReader<>(path, type, CONFIG.get()); }

    // ==================== VALIDATION OPERATIONS ====================

    /**
     * Validates a file and returns detailed results.
     *
     * @param <T> the type to validate against
     * @param path the file path
     * @param type the class to validate against
     * @return validation result with valid rows and errors
     */
    public static <T> ValidationResult<T> validate(String path, Class<T> type) { return validate(Path.of(path), type); }

    /**
     * Validates a file and returns detailed results.
     *
     * @param <T> the type to validate against
     * @param path the file path
     * @param type the class to validate against
     * @return validation result with valid rows and errors
     */
    public static <T> ValidationResult<T> validate(Path path, Class<T> type) {
        validateReadPath(path);
        Format fmt = Format.detect(path.toString());
        if (fmt.isCsv()) return new CsvReader<>(type, CONFIG.get()).validate(path);
        return new ExcelReader<>(type, CONFIG.get()).validate(path);
    }

    // ==================== CONFIGURATION ====================

    /**
     * Sets the global configuration.
     *
     * @param config the configuration to use
     */
    public static void configure(SheetzConfig config) { CONFIG.set(Objects.requireNonNull(config)); }

    /**
     * Gets the current global configuration.
     *
     * @return the current configuration
     */
    public static SheetzConfig config() { return CONFIG.get(); }

    /**
     * Registers a custom converter for a type.
     *
     * @param <T> the type
     * @param type the class
     * @param converter the converter
     */
    public static <T> void register(Class<T> type, Converter<T> converter) { Converters.register(type, converter); }

    /**
     * Resets configuration and converters to defaults.
     */
    public static void reset() { CONFIG.set(SheetzConfig.defaults()); Converters.reset(); MappingCache.clear(); }

    private static void validateReadPath(Path path) {
        Objects.requireNonNull(path, "Path cannot be null");
        if (!Files.exists(path)) throw new SheetzException("File not found: " + path);
    }

    public static final class ReaderBuilder<T> {
        private final Class<T> type; private final SheetzConfig config;
        private Path path; private InputStream inputStream; private Format format;
        private int sheet = 0; private String sheetName; private int headerRow = -1; private char delimiter = ',';

        ReaderBuilder(Class<T> type, SheetzConfig config) { this.type = type; this.config = config; }

        public ReaderBuilder<T> file(String path) { return file(Path.of(path)); }
        public ReaderBuilder<T> file(Path path) { this.path = path; return this; }
        public ReaderBuilder<T> stream(InputStream is, Format fmt) { this.inputStream = is; this.format = fmt; return this; }
        public ReaderBuilder<T> sheet(int index) { this.sheet = index; return this; }
        public ReaderBuilder<T> sheet(String name) { this.sheetName = name; return this; }
        public ReaderBuilder<T> headerRow(int row) { this.headerRow = row; return this; }
        public ReaderBuilder<T> delimiter(char d) { this.delimiter = d; return this; }

        public List<T> read() {
            if (path != null) {
                Format fmt = Format.detect(path.toString());
                if (fmt.isCsv()) return new CsvReader<>(type, config).delimiter(delimiter).read(path);
                ExcelReader<T> reader = new ExcelReader<>(type, config);
                if (sheetName != null) reader.sheet(sheetName); else reader.sheet(sheet);
                if (headerRow >= 0) reader.headerRow(headerRow);
                return reader.read(path);
            }
            if (inputStream != null && format != null) {
                if (format.isCsv()) return new CsvReader<>(type, config).delimiter(delimiter).read(inputStream);
                return new ExcelReader<>(type, config).read(inputStream, format);
            }
            throw new SheetzException("No file or input stream specified");
        }

        public ValidationResult<T> validate() {
            if (path != null) return Sheetz.validate(path, type);
            throw new SheetzException("Validation requires a file path");
        }
    }

    public static final class WriterBuilder<T> {
        private final Class<T> type; private final SheetzConfig config;
        private List<T> data; private Path path; private OutputStream outputStream; private Format format;
        private String sheetName; private boolean autoSize = false; private boolean freezeHeader = false;
        private boolean streaming = false; private char delimiter = ',';

        WriterBuilder(Class<T> type, SheetzConfig config) { this.type = type; this.config = config; }

        public WriterBuilder<T> data(List<T> data) { this.data = data; return this; }
        public WriterBuilder<T> file(String path) { return file(Path.of(path)); }
        public WriterBuilder<T> file(Path path) { this.path = path; return this; }
        public WriterBuilder<T> stream(OutputStream os, Format fmt) { this.outputStream = os; this.format = fmt; return this; }
        public WriterBuilder<T> sheet(String name) { this.sheetName = name; return this; }
        public WriterBuilder<T> autoSize(boolean v) { this.autoSize = v; return this; }
        public WriterBuilder<T> freezeHeader(boolean v) { this.freezeHeader = v; return this; }
        public WriterBuilder<T> streaming(boolean v) { this.streaming = v; return this; }
        public WriterBuilder<T> delimiter(char d) { this.delimiter = d; return this; }

        public void write() {
            if (data == null || data.isEmpty()) throw new SheetzException("No data to write");
            if (path != null) {
                Format fmt = Format.detect(path.toString());
                if (fmt.isCsv()) new CsvWriter<>(type, config).delimiter(delimiter).write(data, path);
                else {
                    ExcelWriter<T> writer = new ExcelWriter<>(type, config).autoSize(autoSize).freezeHeader(freezeHeader).streaming(streaming);
                    if (sheetName != null) writer.sheetName(sheetName);
                    writer.write(data, path, fmt);
                }
                return;
            }
            if (outputStream != null && format != null) {
                if (format.isCsv()) new CsvWriter<>(type, config).delimiter(delimiter).write(data, outputStream);
                else {
                    ExcelWriter<T> writer = new ExcelWriter<>(type, config).autoSize(autoSize).freezeHeader(freezeHeader).streaming(streaming);
                    if (sheetName != null) writer.sheetName(sheetName);
                    writer.write(data, outputStream, format);
                }
                return;
            }
            throw new SheetzException("No output file or stream specified");
        }
    }
}
