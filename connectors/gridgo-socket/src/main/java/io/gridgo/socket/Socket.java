package io.gridgo.socket;

public interface Socket<Payload> {

	boolean isAlive();

	void close();

	int send(Payload message, boolean block);

	default int send(Payload message) {
		return this.send(message, true);
	}
}
