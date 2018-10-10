package io.gridgo.socket.agent.impl;

import io.gridgo.socket.Socket;
import io.gridgo.socket.agent.SocketAgent;
import io.gridgo.utils.helper.AbstractStartable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSocketAgent extends AbstractStartable implements SocketAgent {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Socket socket;

	public AbstractSocketAgent(Socket socket) {
		this.socket = socket;
	}

	public AbstractSocketAgent() {

	}
}
