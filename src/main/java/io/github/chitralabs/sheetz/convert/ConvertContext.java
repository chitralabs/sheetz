package io.github.chitralabs.sheetz.convert;

import io.github.chitralabs.sheetz.SheetzConfig;

public final class ConvertContext {
    private final String fieldName;
    private final Class<?> targetType;
    private final String format;
    private final SheetzConfig config;
    private final int row;
    private final String column;
    
    private ConvertContext(Builder b) {
        this.fieldName = b.fieldName; this.targetType = b.targetType; this.format = b.format;
        this.config = b.config; this.row = b.row; this.column = b.column;
    }
    
    public String fieldName() { return fieldName; }
    public Class<?> targetType() { return targetType; }
    public String format() { return format; }
    public SheetzConfig config() { return config; }
    public int row() { return row; }
    public String column() { return column; }
    public String formatOr(String def) { return (format != null && !format.isEmpty()) ? format : def; }
    
    public static Builder builder() { return new Builder(); }
    
    public static final class Builder {
        private String fieldName; private Class<?> targetType; private String format;
        private SheetzConfig config; private int row = -1; private String column;
        
        public Builder fieldName(String v) { this.fieldName = v; return this; }
        public Builder targetType(Class<?> v) { this.targetType = v; return this; }
        public Builder format(String v) { this.format = v; return this; }
        public Builder config(SheetzConfig v) { this.config = v; return this; }
        public Builder row(int v) { this.row = v; return this; }
        public Builder column(String v) { this.column = v; return this; }
        public ConvertContext build() { return new ConvertContext(this); }
    }
}
