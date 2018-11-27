package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.connector.httpcommon.support.exceptions.UnsupportedFormatException;
import io.gridgo.framework.support.Message;

public interface HttpComponent {

	public default BObject getQueryParams(Message message) {
		if (message.getPayload() == null)
			return BObject.newDefault();
		return message.getPayload().getHeaders().getObject(HttpCommonConsumerConstants.HEADER_QUERY_PARAMS,
				BObject.newDefault());
	}

	public default String getMethod(Message message, String defaultMethod) {
		if (message.getPayload() == null)
			return defaultMethod;
		return message.getPayload().getHeaders().getString(HttpCommonConsumerConstants.HEADER_HTTP_METHOD, defaultMethod);
	}

	public default BElement deserialize(String responseBody) {
		if (responseBody == null)
			return null;
		var format = getFormat();
		if (format == null || format.equals("json"))
			return BElement.fromJson(responseBody);
		if (format.equals("xml"))
			return BElement.fromXml(responseBody);
		throw new UnsupportedFormatException(format);
	}

	public default String serialize(BElement body) {
		if (body == null)
			return null;
		var format = getFormat();
		if (format == null || format.equals("json"))
			return body.toJson();
		if (format.equals("xml"))
			return body.toXml();
		throw new UnsupportedFormatException(format);
	}

	public String getFormat();
}
