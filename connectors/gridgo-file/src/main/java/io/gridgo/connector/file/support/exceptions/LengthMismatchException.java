package io.gridgo.connector.file.support.exceptions;

public class LengthMismatchException extends RuntimeException {

    private static final long serialVersionUID = 4420961627038176981L;

    public LengthMismatchException(int expected, int actual) {
        super("Length mismatch detected. Expected: " + expected + ". Actual: " + actual);
    }
}
