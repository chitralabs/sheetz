package io.github.chitralabs.sheetz;

import io.github.chitralabs.sheetz.exception.SheetzException;
import java.util.Objects;

public enum Format {
    XLSX("xlsx", true),
    XLS("xls", true),
    CSV("csv", false);
    
    private final String extension;
    private final boolean excel;
    
    Format(String extension, boolean excel) {
        this.extension = extension;
        this.excel = excel;
    }
    
    public String extension() { return extension; }
    public boolean isExcel() { return excel; }
    public boolean isCsv() { return this == CSV; }
    
    public static Format detect(String filename) {
        Objects.requireNonNull(filename, "Filename cannot be null");
        String lower = filename.toLowerCase().trim();
        if (lower.endsWith(".xlsx")) return XLSX;
        if (lower.endsWith(".xls")) return XLS;
        if (lower.endsWith(".csv") || lower.endsWith(".tsv")) return CSV;
        throw new SheetzException("Unsupported format: " + filename);
    }
}
