package io.gridgo.connector.message;

import java.util.Map;

import io.gridgo.bean.BValue;

public interface Message {

	/**
	 * routingId use for *-to-1 communication like duplex socket. RoutingId indicate
	 * which endpoint will be the target
	 * 
	 * @return the routing id
	 */
	public BValue getRoutingId();

	public Map<String, Object> getMisc();

	public Payload getPayload();
}
