package io.gridgo.connector.jetty.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BValue;
import io.gridgo.connector.jetty.HttpConstants;
import io.gridgo.connector.jetty.exceptions.HttpRequestParsingException;
import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.connector.jetty.support.HttpHeader;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.helper.Loggable;
import lombok.NonNull;

public abstract class AbstractHttpRequestParser implements HttpRequestParser, Loggable {

	protected static final Set<String> NO_BODY_METHODS = new HashSet<>(Arrays.asList("get", "delete", "options"));

	@Override
	public Message parse(@NonNull HttpServletRequest request, Set<JettyServletContextHandlerOption> options) {
		BObject headers = extractHeaders(request);
		BElement body = extractBody(request);
		Message message = Message.newDefault(Payload.newDefault(headers, body)) //
				.addMisc(HttpConstants.REMOTE_ADDR, request.getRemoteAddr()) //
				.addMisc(HttpConstants.METHOD, request.getMethod()) //
				.addMisc(HttpConstants.SCHEME, request.getScheme()) //
				.addMisc(HttpConstants.CHARACTER_ENCODING, request.getCharacterEncoding()) //
				.addMisc(HttpConstants.COOKIES, request.getCookies()) //
				.addMisc(HttpConstants.CONTENT_LENGTH, request.getContentLengthLong()) //
				.addMisc(HttpConstants.CONTENT_TYPE, request.getContentType()) //
				.addMisc(HttpConstants.CONTENT_PATH, request.getContextPath()) //
				.addMisc(HttpConstants.LOCAL_ADDR, request.getLocalAddr()) //
				.addMisc(HttpConstants.LOCAL_NAME, request.getLocalName()) //
				.addMisc(HttpConstants.LOCALE, request.getLocale()) //
				.addMisc(HttpConstants.LOCALES, request.getLocales()) //
				.addMisc(HttpConstants.SERVER_NAME, request.getServerName()) //
				.addMisc(HttpConstants.SERVER_PORT, request.getServerPort()) //
				.addMisc(HttpConstants.USER_PRINCIPAL, request.getUserPrincipal()) //
				.addMisc(HttpConstants.PATH_INFO, request.getPathInfo()) //
		;
		if (options != null) {
			if (options.contains(JettyServletContextHandlerOption.SESSIONS)) {
				message.addMisc(HttpConstants.SESSION, request.getSession());
			}
		}
		return message;
	}

	protected BObject extractHeaders(HttpServletRequest request) {
		BObject result = BObject.newDefault();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			result.putAny(headerName, request.getHeader(headerName));
		}

		String queryString = request.getQueryString();
		String encoding = request.getCharacterEncoding();
		if (encoding == null || encoding.isBlank()) {
			encoding = "UTF-8";
		}

		// custom extract query string to prevent the request auto parse multipart data
		result.putAny(HttpHeader.QUERY_PARAMS.asString(),
				BObject.newDefault(extractQueryString(queryString, Charset.forName(encoding))));

		result.putAny(HttpHeader.HTTP_METHOD.asString(), request.getMethod());

		return result;
	}

	protected Map<String, String> extractQueryString(String query, Charset charset) {
		Map<String, String> queryPairs = new LinkedHashMap<>();
		if (query != null && !query.isBlank()) {
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				if (pair.isBlank()) {
					continue;
				}

				int idx = pair.indexOf("=");
				if (idx < 0) {
					queryPairs.put(pair, "");
				} else if (idx == 0) {
					queryPairs.put("", URLDecoder.decode(pair.substring(idx + 1), charset));
				} else {
					queryPairs.put(URLDecoder.decode(pair.substring(0, idx), charset),
							URLDecoder.decode(pair.substring(idx + 1), charset));
				}
			}
		}
		return queryPairs;
	}

	protected BElement extractBody(HttpServletRequest request) {
		if (!NO_BODY_METHODS.contains(request.getMethod().toLowerCase().trim())) {
			String contentType = request.getContentType();
			if (contentType != null && contentType.trim().toLowerCase().contains("multipart/form-data")) {
				return extractMultiPartBody(request);
			} else {
				try (InputStream is = request.getInputStream()) {
					return extractInputStreamBody(is);
				} catch (IOException e) {
					throw new HttpRequestParsingException("Error while reading request's body as input stream", e);
				}
			}
		}
		return null;
	}

	protected abstract BElement extractInputStreamBody(InputStream inputStream);

	private BArray extractMultiPartBody(HttpServletRequest request) {
		try {
			BArray results = BArray.newDefault();
			Collection<Part> parts = request.getParts();
			for (Part part : parts) {
				final String contentType = part.getContentType();
				if (contentType.toLowerCase().contains("text/plain")) {
					String[] contentTypeSplitted = contentType.split(";");
					Charset charset = Charset.forName("UTF-8");
					if (contentTypeSplitted.length > 1) {
						String charsetConfig = contentTypeSplitted[1];
						String[] charsetConfigSplitted = charsetConfig.split("=");
						if (charsetConfig.length() >= 2) {
							charset = Charset.forName(charsetConfigSplitted[1].trim());
						}
					}
					results.add(BObject.newDefault() //
							.setAny("name", part.getName()) //
							.setAny(HttpConstants.CONTENT_TYPE, contentTypeSplitted[0]) //
							.setAny(HttpConstants.CHARSET, charset.toString()) //
							.setAny(HttpConstants.IS_CONVERTED, true) //
							.setAny(HttpConstants.VALUE,
									BValue.newDefault(readInputStreamAsString(part.getInputStream(), charset))) //
					);
				} else {
					BObject partHeaders = BObject.newDefault();
					for (String partHeaderName : part.getHeaderNames()) {
						Collection<String> headers = part.getHeaders(partHeaderName);
						partHeaders.putAny(partHeaderName, headers.size() == 1 ? headers.iterator().next() : headers);
					}

					results.add(BObject.newDefault() //
							.setAny(HttpConstants.IS_CONVERTED, false) //
							.setAny(HttpConstants.CONTENT_TYPE, contentType) //
							.setAny(HttpConstants.SIZE, part.getSize()) //
							.setAny("name", part.getName()) //
							.setAny("submittedFileName", part.getSubmittedFileName()) //
							.set("inputStream", BReference.newDefault(part.getInputStream())) //
							.set("headers", partHeaders));
				}
			}
			return results;
		} catch (Exception e) {
			throw new HttpRequestParsingException("Error while parsing multipart/form-data request", e);
		}
	}

	protected String readInputStreamAsString(InputStream inputStream, Charset charset) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(charset);
	}
}
