package io.gridgo.connector.rabbitmq.support.exceptions;

public class ChannelException extends RuntimeException {

    private static final long serialVersionUID = -7198034056261314692L;

    public ChannelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
