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
import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.httpcommon.HttpContentType;

public class HttpEntityHelper {

    public static BArray parseAsMultiPart(Collection<Part> parts) throws IOException {
        BArray results = BArray.ofEmpty();
        for (Part part : parts) {
            final String contentType = part.getContentType();
            if (contentType != null && HttpContentType.isBinaryType(contentType)) {
                results.add(BObject.ofEmpty() //
                                   .setAny(HttpCommonConstants.NAME, part.getName()) //
                                   .setAny(HttpCommonConstants.CONTENT_TYPE, contentType) //
                                   .setAny(HttpCommonConstants.SUBMITTED_FILE_NAME, part.getSubmittedFileName()) //
                                   .setAny(HttpCommonConstants.BODY, BReference.of(part.getInputStream())) //
                );
            } else {
                results.add(BObject.ofEmpty() //
                                   .setAny(HttpCommonConstants.NAME, part.getName()) //
                                   .setAny(HttpCommonConstants.CONTENT_TYPE, contentType)//
                                   .setAny(HttpCommonConstants.BODY, BElement.ofJson(part.getInputStream())) //
                );
            }
        }
        return results;
    }

    public static final BArray parseAsMultiPart(HttpEntity entity) throws IOException {
        return parseAsMultiPart(entity.getContent(), entity.getContentType().getValue());
    }

    public static final BArray parseAsMultiPart(InputStream input, String contentTypeWithBoundary) throws IOException {
        return parseAsMultiPart(new MultiPartFormInputStream(input, contentTypeWithBoundary, null, null).getParts());
    }

    public static String parseAsString(InputStream input) throws IOException {
        return parseAsString(input, Charset.forName("UTF-8"));
    }

    public static String parseAsString(InputStream input, Charset charset) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        input.transferTo(output);
        return output.toString(charset);
    }
}
