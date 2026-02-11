/**
 * Sheetz - High-performance Excel/CSV processing library for Java.
 *
 * <p>Provides annotation-based mapping between spreadsheet rows and Java objects
 * with support for XLSX, XLS, and CSV formats.</p>
 */
module io.github.chitralabs.sheetz.core {
    // Public API exports
    exports io.github.chitralabs.sheetz;
    exports io.github.chitralabs.sheetz.annotation;
    exports io.github.chitralabs.sheetz.convert;
    exports io.github.chitralabs.sheetz.exception;
    exports io.github.chitralabs.sheetz.reader;
    exports io.github.chitralabs.sheetz.writer;

    // Internal packages (not exported)
    // io.github.chitralabs.sheetz.cache is internal

    // Required dependencies
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires com.opencsv;
    requires org.slf4j;
    requires java.xml;
}
