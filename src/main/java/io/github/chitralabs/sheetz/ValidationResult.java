package io.github.chitralabs.sheetz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the outcome of a file validation pass, separating rows that
 * mapped successfully from those that produced errors.
 *
 * <p>Instances are created by {@link Sheetz#validate(String, Class)} and
 * carry the complete list of valid objects, the individual row-level
 * errors, the total row count, and the wall-clock duration of the
 * validation run.</p>
 *
 * <p>{@link #successRate()} returns a percentage between 0 and 100
 * (not a 0-to-1 ratio), so it can be printed directly:</p>
 * <pre>{@code
 * System.out.println("Success: " + result.successRate() + "%");
 * }</pre>
 *
 * @param <T> the model type that rows are validated against
 * @see Sheetz#validate(String, Class)
 */
public final class ValidationResult<T> {
    private final List<T> validRows;
    private final List<RowError> errors;
    private final int totalRows;
    private final long durationMs;
    
    public ValidationResult(List<T> validRows, List<RowError> errors, int totalRows, long durationMs) {
        this.validRows = validRows != null ? new ArrayList<>(validRows) : new ArrayList<>();
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.totalRows = totalRows;
        this.durationMs = durationMs;
    }
    
    public boolean isValid() { return errors.isEmpty(); }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public List<T> validRows() { return Collections.unmodifiableList(validRows); }
    public List<RowError> errors() { return Collections.unmodifiableList(errors); }
    public int totalRows() { return totalRows; }
    public int validCount() { return validRows.size(); }
    public int errorCount() { return errors.size(); }
    public long durationMs() { return durationMs; }
    
    /**
     * Returns the percentage of rows that were valid, as a value
     * between 0.0 and 100.0. If the file contained no data rows,
     * this method returns 100.0 by convention.
     *
     * @return success rate as a percentage (0 to 100)
     */
    public double successRate() { return totalRows > 0 ? (validRows.size() * 100.0 / totalRows) : 100.0; }
    
    public static final class RowError {
        private final int row;
        private final String column;
        private final String message;
        private final Object value;
        private final Throwable cause;
        
        public RowError(int row, String column, String message, Object value, Throwable cause) {
            this.row = row; this.column = column; this.message = message; this.value = value; this.cause = cause;
        }
        public RowError(int row, String message) { this(row, null, message, null, null); }
        
        public int row() { return row; }
        public String column() { return column; }
        public String message() { return message; }
        public Object value() { return value; }
        public Throwable cause() { return cause; }
    }
}
