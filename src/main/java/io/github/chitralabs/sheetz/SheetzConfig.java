package io.github.chitralabs.sheetz;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Immutable configuration for Sheetz operations.
 * 
 * <p>Use the builder to create custom configurations:</p>
 * <pre>{@code
 * SheetzConfig config = SheetzConfig.builder()
 *     .dateFormat("dd/MM/yyyy")
 *     .trimValues(true)
 *     .skipEmptyRows(true)
 *     .streamingThreshold(5000)
 *     .build();
 * 
 * Sheetz.configure(config);
 * }</pre>
 * 
 * <p>All settings have sensible defaults. Use {@link #defaults()} to get the default configuration.</p>
 * 
 * @see Sheetz#configure(SheetzConfig)
 */
public final class SheetzConfig {
    private final String dateFormat;
    private final String dateTimeFormat;
    private final String timeFormat;
    private final boolean trimValues;
    private final boolean skipEmptyRows;
    private final String defaultSheetName;
    private final int headerRow;
    private final int batchSize;
    private final boolean evaluateFormulas;
    private final int streamingThreshold;
    private final Charset charset;
    
    private SheetzConfig(Builder b) {
        this.dateFormat = b.dateFormat;
        this.dateTimeFormat = b.dateTimeFormat;
        this.timeFormat = b.timeFormat;
        this.trimValues = b.trimValues;
        this.skipEmptyRows = b.skipEmptyRows;
        this.defaultSheetName = b.defaultSheetName;
        this.headerRow = b.headerRow;
        this.batchSize = b.batchSize;
        this.evaluateFormulas = b.evaluateFormulas;
        this.streamingThreshold = b.streamingThreshold;
        this.charset = b.charset;
    }
    
    /**
     * Returns the default configuration.
     * @return default configuration
     */
    public static SheetzConfig defaults() { return new Builder().build(); }
    
    /**
     * Creates a new configuration builder.
     * @return new builder
     */
    public static Builder builder() { return new Builder(); }
    
    /** Date format pattern for LocalDate (default: yyyy-MM-dd) */
    public String dateFormat() { return dateFormat; }
    
    /** Date-time format pattern for LocalDateTime (default: yyyy-MM-dd HH:mm:ss) */
    public String dateTimeFormat() { return dateTimeFormat; }
    
    /** Time format pattern for LocalTime (default: HH:mm:ss) */
    public String timeFormat() { return timeFormat; }
    
    /** Whether to trim whitespace from string values (default: true) */
    public boolean trimValues() { return trimValues; }
    
    /** Whether to skip empty rows during reading (default: true) */
    public boolean skipEmptyRows() { return skipEmptyRows; }
    
    /** Default sheet name for writing (default: Sheet1) */
    public String defaultSheetName() { return defaultSheetName; }
    
    /** Header row index, 0-based (default: 0) */
    public int headerRow() { return headerRow; }
    
    /** Batch size for batch processing (default: 1000) */
    public int batchSize() { return batchSize; }
    
    /** Whether to evaluate formulas when reading (default: true) */
    public boolean evaluateFormulas() { return evaluateFormulas; }
    
    /** Row count threshold for auto-enabling streaming writes (default: 10000) */
    public int streamingThreshold() { return streamingThreshold; }
    
    /** Character encoding for CSV file reading and writing (default: UTF-8) */
    public Charset charset() { return charset; }
    
    /**
     * Builder for creating {@link SheetzConfig} instances.
     */
    public static final class Builder {
        private String dateFormat = "yyyy-MM-dd";
        private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        private String timeFormat = "HH:mm:ss";
        private boolean trimValues = true;
        private boolean skipEmptyRows = true;
        private String defaultSheetName = "Sheet1";
        private int headerRow = 0;
        private int batchSize = 1000;
        private boolean evaluateFormulas = true;
        private int streamingThreshold = 10000;
        private Charset charset = StandardCharsets.UTF_8;
        
        /** Sets the date format pattern for LocalDate. */
        public Builder dateFormat(String v) { this.dateFormat = Objects.requireNonNull(v); return this; }
        
        /** Sets the date-time format pattern for LocalDateTime. */
        public Builder dateTimeFormat(String v) { this.dateTimeFormat = Objects.requireNonNull(v); return this; }
        
        /** Sets the time format pattern for LocalTime. */
        public Builder timeFormat(String v) { this.timeFormat = Objects.requireNonNull(v); return this; }
        
        /** Sets whether to trim whitespace from string values. */
        public Builder trimValues(boolean v) { this.trimValues = v; return this; }
        
        /** Sets whether to skip empty rows during reading. */
        public Builder skipEmptyRows(boolean v) { this.skipEmptyRows = v; return this; }
        
        /** Sets the default sheet name for writing. */
        public Builder defaultSheetName(String v) { this.defaultSheetName = Objects.requireNonNull(v); return this; }
        
        /** Sets the header row index (0-based). */
        public Builder headerRow(int v) { if(v<0)throw new IllegalArgumentException("headerRow>=0"); this.headerRow=v; return this; }
        
        /** Sets the batch size for batch processing. */
        public Builder batchSize(int v) { if(v<1)throw new IllegalArgumentException("batchSize>=1"); this.batchSize=v; return this; }
        
        /** Sets whether to evaluate formulas when reading. */
        public Builder evaluateFormulas(boolean v) { this.evaluateFormulas = v; return this; }
        
        /** Sets the row count threshold for auto-enabling streaming writes. */
        public Builder streamingThreshold(int v) { if(v<1)throw new IllegalArgumentException("streamingThreshold>=1"); this.streamingThreshold=v; return this; }
        
        /** Sets the character encoding for CSV file reading and writing. */
        public Builder charset(Charset v) { this.charset = Objects.requireNonNull(v); return this; }
        
        /** Builds the configuration. */
        public SheetzConfig build() { return new SheetzConfig(this); }
    }
}
