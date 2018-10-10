package io.gridgo.socket.agent;

import io.gridgo.socket.Socket;
import io.gridgo.utils.helper.Startable;

public interface SocketAgent extends Startable {

	void setSocket(Socket socket);
}
