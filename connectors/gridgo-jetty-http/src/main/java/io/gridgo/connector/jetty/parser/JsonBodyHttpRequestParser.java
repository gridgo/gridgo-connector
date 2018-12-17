package io.gridgo.connector.jetty.parser;

import static io.gridgo.connector.jetty.support.HttpEntityHelper.parseAsString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BValue;

public class JsonBodyHttpRequestParser extends AbstractHttpRequestParser {

    @Override
    protected BElement extractInputStreamBody(InputStream inputStream) {
        if (inputStream.markSupported()) {
            inputStream.mark(Integer.MAX_VALUE);
        }

        try {
            return BElement.ofJson(inputStream);
        } catch (Exception ex) {
            if (inputStream.markSupported()) {
                try {
                    inputStream.reset();
                    return BValue.of(parseAsString(inputStream, Charset.forName("UTF-8")));
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read inputStream", e);
                }
            } else {
                throw ex;
            }
        }
    }
}
