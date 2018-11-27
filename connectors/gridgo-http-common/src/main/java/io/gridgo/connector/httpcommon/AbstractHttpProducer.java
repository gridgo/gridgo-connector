package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;

public abstract class AbstractHttpProducer extends AbstractProducer {

	private String format;
	
	public AbstractHttpProducer(ConnectorContext context, String format) {
		super(context);
		this.format = format;
	}

	@Override
	public boolean isCallSupported() {
		return true;
	}
	
	protected BObject getQueryParams(Message message) {
		if (message.getPayload() == null)
			return BObject.newDefault();
		return message.getPayload().getHeaders().getObject(HttpCommonConstants.HEADER_QUERY_PARAMS, BObject.newDefault());
	}

	protected String getMethod(Message message, String defaultMethod) {
		if (message.getPayload() == null)
			return defaultMethod;
		return message.getPayload().getHeaders().getString(HttpCommonConstants.HEADER_HTTP_METHOD, defaultMethod);
	}

	protected BElement deserialize(String responseBody) {
		if (responseBody == null)
			return null;
		if ("xml".equals(responseBody))
			return BElement.fromXml(responseBody);
		return BElement.fromJson(responseBody);
	}

	protected String serialize(BElement body) {
		if (body == null)
			return null;
		if ("xml".equals(format))
			return body.toXml();
		return body.toJson();
	}
}
