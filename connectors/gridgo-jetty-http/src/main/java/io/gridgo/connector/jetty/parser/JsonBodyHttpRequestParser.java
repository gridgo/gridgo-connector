package io.gridgo.connector.jetty.parser;

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
			return BElement.fromJson(inputStream);
		} catch (Exception ex) {
			if (inputStream.markSupported()) {
				try {
					inputStream.reset();
					return BValue.newDefault(readInputStreamAsString(inputStream, Charset.forName("UTF-8")));
				} catch (IOException e) {
					throw new RuntimeException("Cannot read inputStream", e);
				}
			} else {
				throw ex;
			}
		}
	}
}
