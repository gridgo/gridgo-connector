package io.gridgo.redis.command.string;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.redis.RedisClient;
import io.gridgo.redis.command.AbstractRedisCommandHandler;
import io.gridgo.redis.command.RedisCommand;
import io.gridgo.redis.command.RedisCommands;
import io.gridgo.redis.exception.IllegalRedisCommandsParamsException;

@RedisCommand(RedisCommands.BITPOS)
public class RedisBitposHandler extends AbstractRedisCommandHandler {

	public RedisBitposHandler() {
		super("key", "state", "start", "end");
	}

	@Override
	protected Promise<BElement, Exception> process(RedisClient redis, BObject options, BElement[] params) {
		switch (params.length) {
		case 2:
			return redis.bitpos(params[0].asValue().getRaw(), params[1].asValue().getBoolean());
		case 3:
			return redis.bitpos(params[0].asValue().getRaw(), params[1].asValue().getBoolean(),
					params[2].asValue().getLong());
		case 4:
			return redis.bitpos(params[0].asValue().getRaw(), params[1].asValue().getBoolean(),
					params[2].asValue().getLong(), params[3].asValue().getLong());
		default:
			throw new IllegalRedisCommandsParamsException("Bitpos input length is not valid.");
		}
	}
}
