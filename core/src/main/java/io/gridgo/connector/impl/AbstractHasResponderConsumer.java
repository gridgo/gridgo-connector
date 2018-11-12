package io.gridgo.connector.impl;

import io.gridgo.connector.HasResponder;
import io.gridgo.connector.Responder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractHasResponderConsumer extends AbstractConsumer implements HasResponder {

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Responder responder;
}
