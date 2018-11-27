package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BValue;
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
		return message.getPayload().getHeaders().getString(HttpCommonConsumerConstants.HEADER_HTTP_METHOD,
				defaultMethod);
	}

	public default BElement deserialize(byte[] responseBody) {
		if (responseBody == null)
			return null;
		var format = getFormat();
		if (format == null || format.equals("json"))
			return BElement.fromJson(new String(responseBody));
		if (format.equals("xml"))
			return BElement.fromXml(new String(responseBody));
		if (format.equals("string"))
			return BValue.newDefault(responseBody);
		if (format.equals("raw"))
			return BElement.fromRaw(responseBody);
		throw new UnsupportedFormatException(format);
	}

	public default byte[] serialize(BElement body) {
		if (body == null)
			return null;
		var format = getFormat();
		if (format == null || format.equals("json"))
			return body.toJson().getBytes();
		if (format.equals("xml"))
			return body.toXml().getBytes();
		if (format.equals("raw"))
			return body.toBytes();
		throw new UnsupportedFormatException(format);
	}

	public String getFormat();
}
