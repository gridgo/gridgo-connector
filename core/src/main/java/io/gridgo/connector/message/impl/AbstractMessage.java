package io.gridgo.connector.message.impl;

import java.util.Map;

import io.gridgo.bean.BValue;
import io.gridgo.connector.message.Message;
import io.gridgo.connector.message.Payload;
import io.gridgo.utils.helper.Assert;
import lombok.Getter;

@Getter
public abstract class AbstractMessage implements Message {

	private BValue routingId;
	private Map<String, Object> misc;
	private Payload payload;

	public void setRoutingId(Object routingId) {
		Assert.notNull(routingId, "routingId");
		if (routingId instanceof BValue) {
			this.routingId = (BValue) routingId;
		} else {
			this.routingId = BValue.newDefault(routingId);
		}
	}

}
