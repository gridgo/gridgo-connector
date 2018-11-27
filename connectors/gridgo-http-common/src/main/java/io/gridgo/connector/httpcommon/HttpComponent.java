package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;

public interface HttpComponent {

	public default BObject getQueryParams(Message message) {
		if (message.getPayload() == null)
			return BObject.newDefault();
		return message.getPayload().getHeaders().getObject(HttpCommonConstants.HEADER_QUERY_PARAMS, BObject.newDefault());
	}

	public default String getMethod(Message message, String defaultMethod) {
		if (message.getPayload() == null)
			return defaultMethod;
		return message.getPayload().getHeaders().getString(HttpCommonConstants.HEADER_HTTP_METHOD, defaultMethod);
	}

	public default BElement deserialize(String responseBody) {
		if (responseBody == null)
			return null;
		if ("xml".equals(getFormat()))
			return BElement.fromXml(responseBody);
		return BElement.fromJson(responseBody);
	}

	public default String serialize(BElement body) {
		if (body == null)
			return null;
		if ("xml".equals(getFormat()))
			return body.toXml();
		return body.toJson();
	}

	public String getFormat();
}
