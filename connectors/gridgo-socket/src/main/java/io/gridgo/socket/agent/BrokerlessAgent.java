package io.gridgo.socket.agent;

import io.gridgo.socket.Socket;
import io.gridgo.utils.helper.Startable;

public interface BrokerlessAgent extends Startable {

	void setSocket(Socket socket);
}
