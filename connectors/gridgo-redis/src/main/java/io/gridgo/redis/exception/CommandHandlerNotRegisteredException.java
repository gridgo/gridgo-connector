package io.gridgo.redis.exception;

public class CommandHandlerNotRegisteredException extends RuntimeException {

    private static final long serialVersionUID = 6609305901650560878L;

    public CommandHandlerNotRegisteredException(String message) {
        super(message);
    }
}
