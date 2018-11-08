package io.gridgo.connector.vertx.support.exceptions;

public class UnsupportedFormatException extends RuntimeException {

	private static final long serialVersionUID = 7283814298148069874L;

	public UnsupportedFormatException(String format) {
		super("Unsupported format: " + format);
	}
}
