package io.gridgo.redis.exception;

public class IllegalRedisCommandsParamsException extends RuntimeException {

    private static final long serialVersionUID = 6712134335085974824L;

    public IllegalRedisCommandsParamsException(String message) {
        super(message);
    }
}
