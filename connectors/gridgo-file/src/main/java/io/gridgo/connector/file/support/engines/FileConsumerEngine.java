package io.gridgo.connector.file.support.engines;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;
import io.gridgo.connector.support.FormattedMarshallable;
import io.gridgo.connector.support.exceptions.UnsupportedFormatException;

public interface FileConsumerEngine extends FormattedMarshallable {

    public default BElement deserialize(byte[] responseBody, int length) {
        if (responseBody == null)
            return null;
        var format = getFormat();
        if (format == null || format.equals("json"))
            return BElement.ofJson(new String(responseBody, 0, length));
        if (format.equals("xml"))
            return BElement.ofXml(new String(responseBody, 0, length));
        if (format.equals("string"))
            return BValue.of(responseBody);
        if (format.equals("raw"))
            return BElement.ofBytes(responseBody);
        throw new UnsupportedFormatException(format);
    }

    public void readAndPublish();
}
