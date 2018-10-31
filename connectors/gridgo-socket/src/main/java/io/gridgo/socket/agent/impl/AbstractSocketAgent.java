package io.gridgo.socket.agent.impl;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.socket.agent.BrokerlessAgent;
import io.gridgo.utils.helper.AbstractStartable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSocketAgent extends AbstractStartable implements BrokerlessAgent {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private BrokerlessSocket socket;

	public AbstractSocketAgent(BrokerlessSocket socket) {
		this.socket = socket;
	}

	public AbstractSocketAgent() {

	}
}
