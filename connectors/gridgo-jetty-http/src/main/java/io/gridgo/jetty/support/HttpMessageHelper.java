package io.gridgo.jetty.support;

import javax.servlet.http.HttpServletRequest;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.NonNull;

public class HttpMessageHelper {

	public static Message parse(@NonNull HttpServletRequest request) {
		BObject headers = null;
		BElement body = null;

		Payload payload = Payload.newDefault(headers, body);
		return Message.newDefault(payload);
	}
}
