package io.github.chitralabs.sheetz.reader;

import io.github.chitralabs.sheetz.*;
import io.github.chitralabs.sheetz.cache.*;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.*;
import com.opencsv.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * True streaming reader for large Excel files using SAX parser.
 * Memory-efficient: processes one row at a time without loading entire file.
 *
 * <p>Implements {@link AutoCloseable} to properly release resources. When closed
 * mid-stream, the background parser thread is interrupted and resources are freed.</p>
 *
 * <p><strong>Security:</strong> The SAX parser is configured with XXE protection
 * (external entity processing is disabled) to prevent XML External Entity attacks
 * from malicious XLSX files.</p>
 *
 * @param <T> the type to map rows to
 */
public final class StreamingReader<T> implements Iterable<T>, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StreamingReader.class);

    private final Path path;
    private final Class<T> type;
    private final SheetzConfig config;
    private final MappingCache.ClassMapping mapping;
    private final Format format;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile Thread parserThread;
    private char csvDelimiter = ',';

    public StreamingReader(Path path, Class<T> type, SheetzConfig config) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.config = Objects.requireNonNull(config, "Config cannot be null");
        this.mapping = MappingCache.get(type);
        this.format = Format.detect(path.toString());
    }

    /**
     * Sets the delimiter character for CSV file streaming.
     * Has no effect on Excel formats.
     *
     * @param delimiter the delimiter character (default: comma)
     * @return this reader for method chaining
     */
    public StreamingReader<T> delimiter(char delimiter) {
        this.csvDelimiter = delimiter;
        return this;
    }

    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "Action cannot be null");
        for (T item : this) {
            action.accept(item);
        }
    }

    public BatchProcessor<T> batch(int size) {
        if (size < 1) throw new IllegalArgumentException("Batch size must be >= 1");
        return new BatchProcessor<>(this, size);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false)
            .onClose(this::close);
    }

    @Override
    public Iterator<T> iterator() {
        if (closed.get()) {
            throw new SheetzException("StreamingReader has been closed");
        }
        if (format == Format.XLSX) {
            return new XlsxStreamingIterator();
        } else if (format == Format.CSV) {
            return new CsvStreamingIterator();
        } else {
            // XLS format - fallback to loading (no streaming API available)
            log.debug("XLS format detected, falling back to in-memory reading");
            return new ExcelReader<>(type, config).read(path).iterator();
        }
    }

    /**
     * Closes the streaming reader and releases all resources.
     * If a parser thread is running, it will be interrupted.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            Thread pt = parserThread;
            if (pt != null && pt.isAlive()) {
                log.debug("Interrupting parser thread for early close");
                pt.interrupt();
                try {
                    pt.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Creates a SAX parser factory with XXE protection enabled.
     * This prevents XML External Entity attacks from malicious XLSX files.
     *
     * @return a securely configured SAXParserFactory
     */
    static SAXParserFactory createSecureSAXParserFactory() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            // XXE Protection: disable external entity processing
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return factory;
        } catch (Exception e) {
            throw new SheetzException("Failed to configure secure XML parser", e);
        }
    }

    /**
     * True SAX-based streaming iterator for XLSX files.
     * Only one row is in memory at a time.
     */
    private class XlsxStreamingIterator implements Iterator<T> {
        private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>(100);
        private final Object POISON = new Object();
        private T nextItem = null;
        private boolean finished = false;
        private volatile Exception parseError = null;

        XlsxStreamingIterator() {
            Thread pt = new Thread(this::parseFile, "sheetz-stream-parser");
            pt.setDaemon(true);
            parserThread = pt;
            pt.start();
            advance();
        }

        private void parseFile() {
            try (OPCPackage pkg = OPCPackage.open(path.toFile())) {
                XSSFReader reader = new XSSFReader(pkg);
                SharedStrings sst = reader.getSharedStringsTable();

                Iterator<InputStream> sheets = reader.getSheetsData();
                if (!sheets.hasNext()) {
                    log.debug("No sheets found in workbook");
                    queue.put(POISON);
                    return;
                }

                try (InputStream sheetStream = sheets.next()) {
                    if (closed.get() || Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    SAXParserFactory factory = createSecureSAXParserFactory();
                    SAXParser parser = factory.newSAXParser();

                    StreamingSheetHandler handler = new StreamingSheetHandler(sst, queue, POISON);
                    parser.parse(sheetStream, handler);
                }

                queue.put(POISON);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.debug("Parser thread interrupted");
            } catch (Exception e) {
                if (!closed.get()) {
                    parseError = e;
                    log.warn("Streaming parse error: {}", e.getMessage());
                }
                try { queue.put(POISON); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        @SuppressWarnings("unchecked")
        private void advance() {
            if (finished) return;
            try {
                Object item = queue.poll(60, TimeUnit.SECONDS);
                if (item == null) {
                    // Timeout - check if parser is still alive
                    if (parserThread != null && !parserThread.isAlive() && queue.isEmpty()) {
                        finished = true;
                        nextItem = null;
                        if (parseError != null) {
                            throw new SheetzException("Streaming parse error", parseError);
                        }
                    } else if (closed.get()) {
                        finished = true;
                        nextItem = null;
                    } else {
                        throw new SheetzException("Streaming read timed out after 60 seconds waiting for next row");
                    }
                } else if (item == POISON) {
                    finished = true;
                    nextItem = null;
                    if (parseError != null) {
                        throw new SheetzException("Streaming parse error", parseError);
                    }
                } else {
                    nextItem = (T) item;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = true;
                nextItem = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextItem != null;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T result = nextItem;
            advance();
            return result;
        }

        /**
         * SAX handler that streams rows one at a time.
         */
        private class StreamingSheetHandler extends DefaultHandler {
            private final SharedStrings sst;
            private final BlockingQueue<Object> queue;
            private final Object poison;

            private List<String> headers;
            private ColumnResolver resolver;
            private List<RowMapper.ResolvedField> fields;

            private int currentRow = -1;
            private int currentCol = -1;
            private Map<Integer, String> rowData;
            private StringBuilder cellValue;
            private boolean inValue = false;
            private boolean isString = false;
            private String cellRef = null;

            StreamingSheetHandler(SharedStrings sst, BlockingQueue<Object> queue, Object poison) {
                this.sst = sst;
                this.queue = queue;
                this.poison = poison;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
                if (closed.get() || Thread.currentThread().isInterrupted()) {
                    throw new SAXException("Parsing cancelled");
                }
                if ("row".equals(qName)) {
                    String rowAttr = attrs.getValue("r");
                    currentRow = rowAttr != null ? Integer.parseInt(rowAttr) - 1 : currentRow + 1;
                    if (currentRow > config.headerRow()) {
                        rowData = new HashMap<>();
                    }
                } else if ("c".equals(qName)) {
                    cellRef = attrs.getValue("r");
                    currentCol = parseCellCol(cellRef);
                    String cellType = attrs.getValue("t");
                    isString = "s".equals(cellType);
                    cellValue = new StringBuilder();
                } else if ("v".equals(qName) || "t".equals(qName)) {
                    inValue = true;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if ("v".equals(qName) || "t".equals(qName)) {
                    inValue = false;
                } else if ("c".equals(qName)) {
                    String value = cellValue.toString();
                    if (isString && !value.isEmpty()) {
                        try {
                            int idx = Integer.parseInt(value);
                            value = new XSSFRichTextString(sst.getItemAt(idx).getString()).toString();
                        } catch (NumberFormatException e) {
                            // Keep as-is
                        }
                    }

                    if (currentRow == config.headerRow()) {
                        if (headers == null) headers = new ArrayList<>();
                        while (headers.size() <= currentCol) headers.add("");
                        headers.set(currentCol, value.trim());
                    } else if (currentRow > config.headerRow() && rowData != null) {
                        rowData.put(currentCol, value);
                    }
                } else if ("row".equals(qName)) {
                    if (currentRow == config.headerRow() && headers != null) {
                        resolver = new ColumnResolver(headers);
                        fields = RowMapper.resolveFields(mapping, resolver);
                    } else if (currentRow > config.headerRow() && rowData != null && fields != null) {
                        try {
                            T obj = RowMapper.mapStringRow(mapping, fields, rowData, currentRow + 1,
                                    headers, config);
                            if (obj != null) {
                                queue.put(obj);
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            log.trace("Skipping invalid row {} in streaming mode: {}", currentRow + 1, e.getMessage());
                        }
                    }
                    rowData = null;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                if (inValue) {
                    cellValue.append(ch, start, length);
                }
            }

            private int parseCellCol(String ref) {
                if (ref == null) return currentCol + 1;
                int col = 0;
                for (char c : ref.toCharArray()) {
                    if (Character.isLetter(c)) {
                        col = col * 26 + (Character.toUpperCase(c) - 'A' + 1);
                    } else {
                        break;
                    }
                }
                return col - 1;
            }
        }
    }

    /**
     * Streaming iterator for CSV files using OpenCSV for correct RFC 4180 parsing.
     * Properly handles embedded newlines, escaped quotes, and BOM markers.
     */
    private class CsvStreamingIterator implements Iterator<T> {
        private final CSVReader csvReader;
        private final List<String> headers;
        private final List<RowMapper.ResolvedField> fields;
        private T nextItem = null;
        private int rowNum = 1;
        private boolean readerClosed = false;

        CsvStreamingIterator() {
            try {
                Reader fileReader = Files.newBufferedReader(path, config.charset());
                this.csvReader = new CSVReaderBuilder(fileReader)
                    .withCSVParser(new CSVParserBuilder()
                        .withSeparator(csvDelimiter)
                        .build())
                    .build();

                String[] headerArr = csvReader.readNext();
                if (headerArr == null) {
                    headers = Collections.emptyList();
                    fields = Collections.emptyList();
                } else {
                    headers = Arrays.asList(headerArr);
                    ColumnResolver resolver = new ColumnResolver(headers);
                    fields = RowMapper.resolveFields(mapping, resolver);
                }
                advance();
            } catch (Exception e) {
                throw new SheetzException("Failed to open CSV file", e);
            }
        }

        private void advance() {
            if (readerClosed) {
                nextItem = null;
                return;
            }
            try {
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    rowNum++;
                    if (config.skipEmptyRows() && isEmpty(line)) continue;

                    T obj = RowMapper.mapStringArray(mapping, fields, line, rowNum,
                            headers, config);
                    if (obj != null) {
                        nextItem = obj;
                        return;
                    }
                }
                nextItem = null;
                closeReader();
            } catch (Exception e) {
                nextItem = null;
                closeReader();
                if (!closed.get()) {
                    log.debug("CSV streaming read error: {}", e.getMessage());
                }
            }
        }

        private void closeReader() {
            if (!readerClosed) {
                readerClosed = true;
                try { csvReader.close(); } catch (IOException ignored) { }
            }
        }

        private boolean isEmpty(String[] line) {
            if (line == null || line.length == 0) return true;
            for (String val : line) {
                if (val != null && !val.trim().isEmpty()) return false;
            }
            return true;
        }

        @Override
        public boolean hasNext() {
            return nextItem != null;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T result = nextItem;
            advance();
            return result;
        }
    }

    /**
     * Batch processor for processing rows in configurable batches.
     */
    public static final class BatchProcessor<T> {
        private final StreamingReader<T> reader;
        private final int batchSize;

        BatchProcessor(StreamingReader<T> reader, int batchSize) {
            this.reader = reader;
            this.batchSize = batchSize;
        }

        public void forEach(Consumer<List<T>> action) {
            List<T> batch = new ArrayList<>(batchSize);
            for (T item : reader) {
                batch.add(item);
                if (batch.size() >= batchSize) {
                    action.accept(new ArrayList<>(batch));
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                action.accept(batch);
            }
        }
    }
}
