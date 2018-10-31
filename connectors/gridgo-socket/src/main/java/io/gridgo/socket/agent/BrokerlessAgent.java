package io.gridgo.socket.agent;

import io.gridgo.socket.BrokerlessSocket;
import io.gridgo.utils.helper.Startable;

public interface BrokerlessAgent extends Startable {

	void setSocket(BrokerlessSocket socket);
}
