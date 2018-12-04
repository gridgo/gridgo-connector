package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BObject;
import io.gridgo.connector.support.FormattedMarshallable;
import io.gridgo.framework.support.Message;

public interface HttpComponent extends FormattedMarshallable {

	public default BObject getQueryParams(Message message) {
		if (message.getPayload() == null)
			return BObject.ofEmpty();
		return message.getPayload().getHeaders().getObject(HttpCommonConsumerConstants.HEADER_QUERY_PARAMS,
				BObject.ofEmpty());
	}

	public default String getMethod(Message message, String defaultMethod) {
		if (message.getPayload() == null)
			return defaultMethod;
		return message.getPayload().getHeaders().getString(HttpCommonConsumerConstants.HEADER_HTTP_METHOD,
				defaultMethod);
	}
}
