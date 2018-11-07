package io.gridgo.connector.support;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class ConnectionRef<T> {
	
	private T connection;
	
	private AtomicInteger refCount = new AtomicInteger(0);

	public ConnectionRef(T connection) {
		this.connection = connection;
	}
	
	public int getRefCount() {
		return refCount.get();
	}
	
	public int ref() {
		return refCount.incrementAndGet();
	}
	
	public int deref() {
		return refCount.decrementAndGet();
	}
}
