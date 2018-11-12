package io.gridgo.socket.netty4;

import java.util.HashSet;
import java.util.Set;

public enum Netty4Transport {

	TCP, UDT, WEBSOCKET("ws");

	private final Set<String> aliases = new HashSet<>();

	private Netty4Transport() {
		this.aliases.add(this.name().toLowerCase());
	}

	private Netty4Transport(String... aliases) {
		this();
		if (aliases != null && aliases.length > 0) {
			for (String alias : aliases) {
				this.aliases.add(alias.toLowerCase());
			}
		}
	}

	public static final Netty4Transport fromName(String name) {
		if (name != null) {
			name = name.toLowerCase();
			for (Netty4Transport value : values()) {
				if (value.aliases.contains(name)) {
					return value;
				}
			}
		}
		return null;
	}
}
