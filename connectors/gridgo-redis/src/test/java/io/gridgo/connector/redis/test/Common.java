package io.gridgo.connector.redis.test;

import org.joo.promise4j.Promise;

import io.gridgo.framework.support.Message;

public class Common {
	public static Promise<Message, Exception> checkStringResult(Message msg, String expected) {
		String res = msg.body().asValue().getString();
		System.out.println("res string: " + res);
		if (res.equals(expected))
			return Promise.of(msg);
		return Promise.ofCause(new RuntimeException());
	}

	public static Promise<Message, Exception> checkLongResult(Message msg, long expected) {
		long res = msg.body().asValue().getLong();
		System.out.println("res: " + res);
		if (res == expected)
			return Promise.of(msg);
		return Promise.ofCause(new RuntimeException());
	}
}
