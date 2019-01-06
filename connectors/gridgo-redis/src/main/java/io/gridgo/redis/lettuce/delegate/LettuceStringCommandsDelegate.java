package io.gridgo.redis.lettuce.delegate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.redis.command.RedisStringCommands;
import io.gridgo.utils.PrimitiveUtils;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.BitFieldArgs.OverflowType;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

public interface LettuceStringCommandsDelegate extends LettuceCommandsDelegate, RedisStringCommands {

    @Override
    default Promise<BElement, Exception> append(byte[] key, byte[] value) {
        return toPromise(getStringCommands().append(key, value));
    }

    @Override
    default Promise<BElement, Exception> bitcount(byte[] key, long start, long end) {
        return toPromise(getStringCommands().bitcount(key, start, end));
    }

    static final Set<String> BITFIELD_SUB_COMMANDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("set", "get", "incrby")));

    @Override
    default Promise<BElement, Exception> bitfield(byte[] key, String overflow, Object... subCommandAndArgs) {
        final BitFieldArgs bitFieldArgs = new BitFieldArgs();
        if (overflow != null)
            bitFieldArgs.overflow(OverflowType.valueOf(overflow.trim().toUpperCase()));
        if (subCommandAndArgs == null || subCommandAndArgs.length == 0)
            return toPromise(getStringCommands().bitfield(key, bitFieldArgs));
        var stack = new Stack<>();
        for (int i = subCommandAndArgs.length - 1; i >= 0; i--) {
            stack.push(subCommandAndArgs[i]);
        }

        var cmdAndArgs = new LinkedList<>();
        while (!stack.isEmpty()) {
            var head = stack.pop();
            if (cmdAndArgs.isEmpty() || !BITFIELD_SUB_COMMANDS.contains(head)) {
                cmdAndArgs.add(head);
            } else {
                // process cmdAndArgs
                processSubCommand(bitFieldArgs, cmdAndArgs);
                // renew cmdAndArgs
                cmdAndArgs = new LinkedList<>();
                cmdAndArgs.add(head);
            }
        }
        if (cmdAndArgs.size() > 0)
            processSubCommand(bitFieldArgs, cmdAndArgs);
        return toPromise(getStringCommands().bitfield(key, bitFieldArgs));
    }

    @Override
    default Promise<BElement, Exception> bitopAnd(byte[] destination, byte[]... keys) {
        return toPromise(getStringCommands().bitopAnd(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> bitopNot(byte[] destination, byte[] source) {
        return toPromise(getStringCommands().bitopNot(destination, source));
    }

    @Override
    default Promise<BElement, Exception> bitopOr(byte[] destination, byte[]... keys) {
        return toPromise(getStringCommands().bitopOr(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> bitopXor(byte[] destination, byte[]... keys) {
        return toPromise(getStringCommands().bitopXor(destination, keys));
    }

    @Override
    default Promise<BElement, Exception> bitpos(byte[] key, boolean state) {
        return toPromise(getStringCommands().bitpos(key, state));
    }

    @Override
    default Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start) {
        return toPromise(getStringCommands().bitpos(key, state, start));
    }

    @Override
    default Promise<BElement, Exception> bitpos(byte[] key, boolean state, long start, long end) {
        return toPromise(getStringCommands().bitpos(key, state, start, end));
    }

    @Override
    default Promise<BElement, Exception> decr(byte[] key) {
        return toPromise(getStringCommands().decr(key));
    }

    @Override
    default Promise<BElement, Exception> decrby(byte[] key, long amount) {
        return toPromise(getStringCommands().decrby(key, amount));
    }

    @Override
    default Promise<BElement, Exception> get(byte[] key) {
        return toPromise(getStringCommands().get(key));
    }

    @Override
    default Promise<BElement, Exception> getbit(byte[] key, long offset) {
        return toPromise(getStringCommands().getbit(key, offset));
    }

    @Override
    default Promise<BElement, Exception> getrange(byte[] key, long start, long end) {
        return toPromise(getStringCommands().getrange(key, start, end));
    }

    @Override
    default Promise<BElement, Exception> getset(byte[] key, byte[] value) {
        return toPromise(getStringCommands().getset(key, value));
    }

    <T extends RedisStringAsyncCommands<byte[], byte[]>> T getStringCommands();

    @Override
    default Promise<BElement, Exception> incr(byte[] key) {
        return toPromise(getStringCommands().incr(key));
    }

    @Override
    default Promise<BElement, Exception> incrby(byte[] key, long amount) {
        return toPromise(getStringCommands().incrby(key, amount));
    }

    @Override
    default Promise<BElement, Exception> incrbyfloat(byte[] key, double amount) {
        return toPromise(getStringCommands().incrbyfloat(key, amount));
    }

    @Override
    default Promise<BElement, Exception> mget(byte[]... keys) {
        return toPromise(getStringCommands().mget(keys) //
                                            .thenApply(list -> this.convertList(list, this::keyValueToBArray)));
    }

    @Override
    default Promise<BElement, Exception> mset(Map<byte[], byte[]> map) {
        return toPromise(getStringCommands().mset(map));
    }

    @Override
    default Promise<BElement, Exception> msetnx(Map<byte[], byte[]> map) {
        return toPromise(getStringCommands().msetnx(map));
    }

    default void processGetCommand(final BitFieldArgs bitFieldArgs, List<Object> cmdAndArgs) {
        if (cmdAndArgs.size() == 2) {
            String type = cmdAndArgs.remove(0).toString();
            int bits = Integer.parseInt(type.substring(1));
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            if (type.startsWith("u")) {
                bitFieldArgs.get(BitFieldArgs.unsigned(bits), offset);
            } else {
                bitFieldArgs.get(BitFieldArgs.signed(bits), offset);
            }
        } else if (cmdAndArgs.size() == 1) {
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            bitFieldArgs.get(offset);
        } else if (cmdAndArgs.size() == 0) {
            bitFieldArgs.get();
        }
    }

    default void processIncreaseCommand(final BitFieldArgs bitFieldArgs, List<Object> cmdAndArgs) {
        if (cmdAndArgs.size() == 3) {
            String type = cmdAndArgs.remove(0).toString();
            int bits = Integer.parseInt(type.substring(1));
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            if (type.startsWith("u")) {
                bitFieldArgs.incrBy(BitFieldArgs.unsigned(bits), offset, value);
            } else {
                bitFieldArgs.incrBy(BitFieldArgs.signed(bits), offset, value);
            }
        } else if (cmdAndArgs.size() == 2) {
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            bitFieldArgs.incrBy(offset, value);
        } else if (cmdAndArgs.size() == 1) {
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            bitFieldArgs.incrBy(value);
        }
    }

    default void processSetCommand(final BitFieldArgs bitFieldArgs, List<Object> cmdAndArgs) {
        if (cmdAndArgs.size() == 3) {
            String type = cmdAndArgs.remove(0).toString();
            int bits = Integer.parseInt(type.substring(1));
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            if (type.startsWith("u")) {
                bitFieldArgs.set(BitFieldArgs.unsigned(bits), offset, value);
            } else {
                bitFieldArgs.set(BitFieldArgs.signed(bits), offset, value);
            }
        } else if (cmdAndArgs.size() == 2) {
            int offset = PrimitiveUtils.getIntegerValueFrom(cmdAndArgs.remove(0));
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            bitFieldArgs.set(offset, value);
        } else if (cmdAndArgs.size() == 1) {
            long value = PrimitiveUtils.getLongValueFrom(cmdAndArgs.remove(0));
            bitFieldArgs.set(value);
        }
    }

    default void processSubCommand(final BitFieldArgs bitFieldArgs, List<Object> cmdAndArgs) {
        String cmd = cmdAndArgs.remove(0).toString();
        switch (cmd.trim().toLowerCase()) {
        case "get":
            processGetCommand(bitFieldArgs, cmdAndArgs);
            break;
        case "set":
            processSetCommand(bitFieldArgs, cmdAndArgs);
            break;
        case "incrby":
            processIncreaseCommand(bitFieldArgs, cmdAndArgs);
            break;
        default:
        }
    }

    @Override
    default Promise<BElement, Exception> psetex(byte[] key, long milliseconds, byte[] value) {
        return toPromise(getStringCommands().psetex(key, milliseconds, value));
    }

    @Override
    default Promise<BElement, Exception> set(byte[] key, byte[] value) {
        return toPromise(getStringCommands().set(key, value));
    }

    @Override
    default Promise<BElement, Exception> setbit(byte[] key, long offset, int value) {
        return toPromise(getStringCommands().setbit(key, offset, value));
    }

    @Override
    default Promise<BElement, Exception> setex(byte[] key, long seconds, byte[] value) {
        return toPromise(getStringCommands().setex(key, seconds, value));
    }

    @Override
    default Promise<BElement, Exception> setnx(byte[] key, byte[] value) {
        return toPromise(getStringCommands().setnx(key, value));
    }

    @Override
    default Promise<BElement, Exception> setrange(byte[] key, long offset, byte[] value) {
        return toPromise(getStringCommands().setrange(key, offset, value));
    }

    @Override
    default Promise<BElement, Exception> strlen(byte[] key) {
        return toPromise(getStringCommands().strlen(key));
    }
}
