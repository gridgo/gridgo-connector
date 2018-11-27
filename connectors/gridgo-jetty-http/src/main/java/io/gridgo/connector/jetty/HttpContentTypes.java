package io.gridgo.connector.jetty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum HttpContentTypes {

	TEXT_PLAIN("text/plain"), //

	APPLICATION_OCTET_STREAM("application/octet-stream"), //

	APPLICATION_JSON("application/json"), //

	MULTIPART_FORM_DATA("multipart/form-data") //

	;

	@Getter
	private final String value;

	public static final boolean isSupported(String value) {
		return forValue(value) != null;
	}

	public static final HttpContentTypes forValue(String value) {
		if (value != null) {
			for (HttpContentTypes contentType : values()) {
				if (contentType.getValue().equalsIgnoreCase(value)) {
					return contentType;
				}
			}
		}
		return null;
	}

	public static final HttpContentTypes forValueOrDefault(String value, HttpContentTypes defaultValue) {
		var result = forValue(value);
		return result == null ? defaultValue : result;
	}
}
