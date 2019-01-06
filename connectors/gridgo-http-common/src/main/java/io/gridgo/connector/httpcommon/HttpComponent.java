package io.gridgo.connector.httpcommon;

import io.gridgo.bean.BObject;
import io.gridgo.connector.support.FormattedMarshallable;
import io.gridgo.framework.support.Message;

public interface HttpComponent extends FormattedMarshallable {

    public default String getMethod(Message message, String defaultMethod) {
        if (message.getPayload() == null)
            return defaultMethod;
        return message.headers().getString(HttpCommonConsumerConstants.HEADER_HTTP_METHOD, defaultMethod);
    }

    public default BObject getQueryParams(Message message) {
        if (message.getPayload() == null)
            return BObject.ofEmpty();
        return message.headers().getObject(HttpCommonConsumerConstants.HEADER_QUERY_PARAMS, BObject.ofEmpty());
    }
}
