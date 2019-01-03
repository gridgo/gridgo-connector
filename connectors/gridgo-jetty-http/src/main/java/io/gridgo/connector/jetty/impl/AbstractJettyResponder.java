package io.gridgo.connector.jetty.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.bean.BReference;
import io.gridgo.bean.BType;
import io.gridgo.bean.BValue;
import io.gridgo.connector.httpcommon.AbstractTraceableResponder;
import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.httpcommon.HttpContentType;
import io.gridgo.connector.httpcommon.HttpHeader;
import io.gridgo.connector.httpcommon.HttpStatus;
import io.gridgo.connector.httpcommon.support.DeferredAndRoutingId;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.utils.exception.RuntimeIOException;
import io.gridgo.utils.wrapper.ByteBufferInputStream;
import lombok.NonNull;

public class AbstractJettyResponder extends AbstractTraceableResponder implements JettyResponder {

    private static final AtomicLong ID_SEED = new AtomicLong(0);

    private Function<Throwable, Message> failureHandler = this::generateFailureMessage;

    private final String uniqueIdentifier;

    protected AbstractJettyResponder(ConnectorContext context, @NonNull String uniqueIdentifier) {
        super(context);
        this.uniqueIdentifier = uniqueIdentifier;
    }

    private InputStream createInputStream(BElement body) {
        if (!(body instanceof BReference))
            return null;
        var obj = body.asReference().getReference();
        if (obj instanceof InputStream)
            return (InputStream) obj;
        if (obj instanceof ByteBuffer)
            return new ByteBufferInputStream((ByteBuffer) obj);
        if (obj instanceof byte[])
            return new ByteArrayInputStream((byte[]) obj);
        if (obj instanceof File || obj instanceof Path) {
            File file = obj instanceof File ? (File) obj : ((Path) obj).toFile();
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                handleException(e);
            }
        }
        return null;
    }

    @Override
    public Message generateFailureMessage(Throwable ex) {
        // print exception anyway
        getLogger().error("Error while handling request", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR_500;
        BElement body = BValue.of(status.getDefaultMessage());

        Payload payload = Payload.of(body).addHeader(HttpCommonConstants.HEADER_STATUS, status.getCode());
        return Message.of(payload);
    }

    @Override
    protected String generateName() {
        return "producer.jetty.http-server." + this.uniqueIdentifier;
    }

    private InputStream getInputStreamFromBody(Object obj) {
        if (obj instanceof InputStream)
            return (InputStream) obj;
        if (obj instanceof ByteBuffer)
            return new ByteBufferInputStream((ByteBuffer) obj);
        if (obj instanceof byte[])
            return new ByteArrayInputStream((byte[]) obj);
        return null;
    }

    protected void handleException(Throwable e) {
        var exceptionHandler = getContext().getExceptionHandler();
        if (exceptionHandler != null) {
            exceptionHandler.accept(e);
        } else {
            getLogger().error("Exception caught", e);
        }
    }

    protected String lookUpResponseHeader(@NonNull String headerName) {
        HttpHeader httpHeader = HttpHeader.lookUp(headerName.toLowerCase());
        if (httpHeader != null && httpHeader.isForResponse() && !httpHeader.isCustom()) {
            return httpHeader.asString();
        }
        return null;
    }

    private HttpContentType parseContentType(BElement body, String headerSetContentType) {
        var contentType = HttpContentType.forValue(headerSetContentType);

        if (contentType == null) {
            if (body instanceof BValue) {
                contentType = HttpContentType.DEFAULT_TEXT;
            } else if (body instanceof BReference) {
                var ref = body.asReference().getReference();
                if (ref instanceof File || ref instanceof Path) {
                    contentType = HttpContentType.forFile(ref instanceof File ? (File) ref : ((Path) ref).toFile());
                } else {
                    contentType = HttpContentType.DEFAULT_BINARY;
                }
            } else {
                contentType = HttpContentType.DEFAULT_JSON;
            }
        }
        return contentType;
    }

    @Override
    public DeferredAndRoutingId registerRequest(@NonNull HttpServletRequest request) {
        final Deferred<Message, Exception> deferredResponse = new CompletableDeferredObject<>();
        final AsyncContext asyncContext = request.startAsync();
        final long routingId = ID_SEED.getAndIncrement();
        this.deferredResponses.put(routingId, deferredResponse);
        deferredResponse.promise().always((stt, resp, ex) -> {
            try {
                HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
                Message responseMessage = stt == DeferredStatus.RESOLVED ? resp : this.failureHandler.apply(ex);
                writeResponse(response, responseMessage);
            } catch (Exception e) {
                handleException(e);
            } finally {
                deferredResponses.remove(routingId);
                asyncContext.complete();
            }
        });
        return DeferredAndRoutingId.builder().deferred(deferredResponse).routingId(BValue.of(routingId)).build();
    }

    @Override
    protected void send(Message message, Deferred<Message, Exception> deferredAck) {
        super.resolveTraceable(message, deferredAck);
    }

    private void sendResponse(HttpServletResponse response, BObject headers, BElement body, HttpContentType contentType) {
        if (contentType != HttpContentType.MULTIPART_FORM_DATA || body == null) {
            this.writeHeaders(headers, response);
        }

        /* ------------------------------------------ */
        /**
         * process body
         */
        if (body != null) {
            if (contentType.isJsonFormat()) {
                this.writeBodyJson(body, response);
            } else if (contentType.isBinaryFormat()) {
                this.writeBodyBinary(body, response);
            } else if (contentType.isMultipartFormat()) {
                this.writeBodyMultipart(body, response, contentTypeWithBoundary -> {
                    headers.setAny(HttpCommonConstants.CONTENT_TYPE, contentTypeWithBoundary);
                    this.writeHeaders(headers, response);
                });
            } else {
                this.writeBodyTextPlain(body, response);
            }
        }
    }

    @Override
    public JettyResponder setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    protected void takeOutputStream(HttpServletResponse response, Consumer<ServletOutputStream> osConsumer) {
        ServletOutputStream outputStream = null;

        try {
            outputStream = response.getOutputStream();
        } catch (IOException e) {
            handleException(e);
            return;
        }

        try {
            osConsumer.accept(outputStream);
        } finally {
            try {
                outputStream.flush();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    protected void takeWriter(HttpServletResponse response, Consumer<PrintWriter> writerConsumer) {
        PrintWriter writer = null;

        try {
            writer = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }

        try {
            writerConsumer.accept(writer);
        } finally {
            writer.flush();
        }
    }

    protected void writeBodyBinary(BElement body, HttpServletResponse response) {
        writeBodyBinary(body, response, null);
    }

    protected void writeBodyBinary(BElement body, HttpServletResponse response, LongConsumer contentLengthConsumer) {
        takeOutputStream(response, output -> {
            var inputStream = createInputStream(body);
            if (inputStream != null) {
                try (var is = inputStream) {
                    if (contentLengthConsumer != null) {
                        contentLengthConsumer.accept((long) is.available());
                    }
                    is.transferTo(output);
                } catch (Exception e) {
                    handleException(e);
                }
            } else {
                body.writeBytes(output);
            }
        });
    }

    protected void writeBodyJson(BElement body, HttpServletResponse response) {
        if (body instanceof BValue) {
            writeBodyTextPlain(body, response);
        } else if (body instanceof BReference) {
            writeBodyBinary(body, response);
        } else {
            takeWriter(response, body::writeJson);
        }
    }

    protected void writeBodyMultipart(@NonNull BElement body, @NonNull HttpServletResponse response, @NonNull Consumer<String> contentTypeConsumer) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (body instanceof BObject) {
            for (Entry<String, BElement> entry : body.asObject().entrySet()) {
                String name = entry.getKey();
                writePart(name, entry.getValue(), builder);
            }
        } else if (body instanceof BArray) {
            for (BElement entry : body.asArray()) {
                writePart(null, entry, builder);
            }
        } else {
            writePart(null, body, builder);
        }

        takeOutputStream(response, outstream -> {
            try {
                HttpEntity entity = builder.build();
                contentTypeConsumer.accept(entity.getContentType().getValue());
                entity.writeTo(outstream);
            } catch (IOException e) {
                handleException(new RuntimeIOException(e));
            }
        });

    }

    protected void writeBodyTextPlain(BElement body, HttpServletResponse response) {
        if (body instanceof BReference) {
            writeBodyBinary(body, response, //
                    contentLength -> response.addHeader(HttpCommonConstants.CONTENT_LENGTH, String.valueOf(contentLength)));
        } else {
            takeWriter(response, writer -> writer.write(body.toJson()));
        }
    }

    protected void writeHeaders(@NonNull BObject headers, @NonNull HttpServletResponse response) {
        for (var entry : headers.entrySet()) {
            if (entry.getValue().isValue() && !entry.getValue().asValue().isNull()) {
                var stdHeaderName = lookUpResponseHeader(entry.getKey());
                if (stdHeaderName != null) {
                    response.addHeader(stdHeaderName, entry.getValue().asValue().getString());
                }
            }
        }
    }

    protected void writePart(String name, BElement value, MultipartEntityBuilder builder) {
        name = name == null ? "" : name;
        if (value instanceof BValue) {
            writeValue(name, value, builder);
        } else if (value instanceof BReference) {
            writeReference(name, value, builder);
        } else {
            builder.addPart(name, new StringBody(value.toJson(), ContentType.APPLICATION_JSON));
        }
    }

    private void writeReference(String name, BElement value, MultipartEntityBuilder builder) {
        var obj = value.asReference().getReference();
        if (obj instanceof File || obj instanceof Path) {
            var file = obj instanceof File ? (File) obj : ((Path) obj).toFile();
            builder.addBinaryBody(name, file);
            return;
        }
        var inputStream = getInputStreamFromBody(obj);

        if (inputStream != null) {
            builder.addBinaryBody(name, inputStream);
        } else {
            handleException(new IllegalArgumentException("cannot make input stream from BReferrence"));
        }
    }

    @Override
    public void writeResponse(HttpServletResponse response, Message message) {
        /* -------------------------------------- */
        /**
         * process header
         */
        BObject headers = message.getPayload().getHeaders();
        BElement body = message.getPayload().getBody();

        var headerSetContentType = headers.getString(HttpCommonConstants.CONTENT_TYPE, null);
        var contentType = parseContentType(body, headerSetContentType);

        int statusCode = headers.getInteger(HttpCommonConstants.HEADER_STATUS, HttpStatus.OK_200.getCode());
        response.setStatus(statusCode);

        if (contentType.isTextFormat()) {
            String charset = headers.getString(HttpCommonConstants.CHARSET, "UTF-8");
            response.setCharacterEncoding(charset);
        }

        if (!headers.containsKey(HttpCommonConstants.CONTENT_TYPE)) {
            headers.setAny(HttpCommonConstants.CONTENT_TYPE, contentType.getMime());
        }

        sendResponse(response, headers, body, contentType);
    }

    private void writeValue(String name, BElement value, MultipartEntityBuilder builder) {
        if (value.getType() == BType.RAW) {
            builder.addBinaryBody(name, value.asValue().getRaw());
        } else {
            builder.addTextBody(name, value.asValue().getString());
        }
    }
}
