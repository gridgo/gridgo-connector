package io.gridgo.connector.support.exceptions;

public class SendMessageException extends Exception {

	public SendMessageException(Exception e) {
		super(e);
	}

	public SendMessageException() {
		super();
	}

	private static final long serialVersionUID = -248588838925175405L;
}
