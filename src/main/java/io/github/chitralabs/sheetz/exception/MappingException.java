package io.github.chitralabs.sheetz.exception;

public class MappingException extends SheetzException {
    private final int row;
    private final String column;
    private final Object value;
    
    public MappingException(String message, int row) { super(message); this.row = row; this.column = null; this.value = null; }
    public MappingException(String message, int row, String column, Object value) { super(message); this.row = row; this.column = column; this.value = value; }
    public MappingException(String message, int row, String column, Object value, Throwable cause) { super(message, cause); this.row = row; this.column = column; this.value = value; }
    
    public int row() { return row; }
    public String column() { return column; }
    public Object value() { return value; }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Row ").append(row);
        if (column != null) sb.append(", column '").append(column).append("'");
        sb.append(": ").append(super.getMessage());
        if (value != null) sb.append(" (value: ").append(value).append(")");
        return sb.toString();
    }
}
