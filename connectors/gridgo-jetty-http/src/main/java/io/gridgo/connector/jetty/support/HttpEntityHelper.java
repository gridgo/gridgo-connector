package io.gridgo.connector.jetty.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.servlet.http.Part;

import org.apache.http.HttpEntity;
import org.eclipse.jetty.http.MultiPartFormInputStream;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;

public class HttpEntityHelper {

	public static final BArray parseAsMultiPart(HttpEntity entity) throws IOException {
		return parseAsMultiPart(entity.getContent(), entity.getContentType().getValue());
	}

	public static final BArray parseAsMultiPart(InputStream input, String contentTypeWithBoundary) throws IOException {
		return parseAsMultiPart(new MultiPartFormInputStream(input, contentTypeWithBoundary, null, null).getParts());
	}

	public static BArray parseAsMultiPart(Collection<Part> parts) throws IOException {
		BArray results = BArray.newDefault();
		for (Part part : parts) {
			final String contentType = part.getContentType();
			if (!contentType.toLowerCase().contains(HttpContentType.APPLICATION_OCTET_STREAM.getMime())) {
				results.add(BObject.newDefault() //
						.setAny(HttpConstants.NAME, part.getName()) //
						.setAny(HttpConstants.CONTENT_TYPE, contentType)//
						.setAny(HttpConstants.BODY, BElement.fromJson(part.getInputStream())) //
				);
			} else {
				results.add(BObject.newDefault() //
						.setAny(HttpConstants.NAME, part.getName()) //
						.setAny(HttpConstants.CONTENT_TYPE, contentType) //
						.setAny(HttpConstants.SUBMITTED_FILE_NAME, part.getSubmittedFileName()) //
						.setAny(HttpConstants.BODY, BReference.newDefault(part.getInputStream())) //
				);
			}
		}
		return results;
	}

	public static String parseAsString(InputStream input, Charset charset) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		input.transferTo(output);
		return output.toString(charset);
	}

	public static String parseAsString(InputStream input) throws IOException {
		return parseAsString(input, Charset.forName("UTF-8"));
	}
}
