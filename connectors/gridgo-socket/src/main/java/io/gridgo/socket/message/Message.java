package io.gridgo.socket.message;

import java.util.Map;

import io.gridgo.bean.BValue;

public interface Message {

	/**
	 * routingId use for *-to-1 communication like duplex socket. RoutingId indicate
	 * which endpoint will be the target
	 * 
	 * @return the routing id
	 */
	BValue getRoutingId();

	Map<String, Object> getMisc();

	Payload getPayload();
}
