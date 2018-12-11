package io.gridgo.connector.support;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.connector.support.exceptions.UnsupportedFormatException;

public interface FormattedMarshallable {

    public default BElement deserialize(byte[] responseBody) {
        if (responseBody == null || responseBody.length == 0)
            return null;
        var format = getFormat();
        if (format == null || format.equals("json"))
            return BElement.fromJson(new String(responseBody));
        if (format.equals("xml"))
            return BElement.fromXml(new String(responseBody));
        if (format.equals("string"))
            return BValue.of(responseBody);
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
