package io.github.chitralabs.sheetz.exception;

public class SheetzException extends RuntimeException {
    public SheetzException(String message) { super(message); }
    public SheetzException(String message, Throwable cause) { super(message, cause); }
    public SheetzException(Throwable cause) { super(cause); }
}
