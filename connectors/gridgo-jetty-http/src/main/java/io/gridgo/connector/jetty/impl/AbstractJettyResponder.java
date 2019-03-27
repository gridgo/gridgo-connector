package io.gridgo.connector.jetty.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.eclipse.jetty.server.HttpOutput;
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

    private final boolean mmapEnabled;
    private final String format;

    protected AbstractJettyResponder(ConnectorContext context, boolean mmapEnabled, String format, @NonNull String uniqueIdentifier) {
        super(context);
        this.format = format;
        this.uniqueIdentifier = uniqueIdentifier;
        this.mmapEnabled = mmapEnabled;
    }

    @Override
    public Message generateFailureMessage(Throwable ex) {
        // print exception anyway
        getLogger().error("Error while handling request", ex);

        var status = HttpStatus.INTERNAL_SERVER_ERROR_500;
        var body = BValue.of(status.getDefaultMessage());

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
        var httpHeader = HttpHeader.lookUp(headerName.toLowerCase());
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
        var deferredResponse = new CompletableDeferredObject<Message, Exception>();
        var asyncContext = request.startAsync();
        var routingId = ID_SEED.getAndIncrement();
        this.deferredResponses.put(routingId, deferredResponse);
        deferredResponse.promise().always((stt, resp, ex) -> {
            try {
                var response = (HttpServletResponse) asyncContext.getResponse();
                var responseMessage = stt == DeferredStatus.RESOLVED ? resp : this.failureHandler.apply(ex);
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

    protected void takeWriter(HttpServletResponse response, Consumer<PrintWriter> osConsumer) {
        PrintWriter writer = null;

        try {
            writer = response.getWriter();
        } catch (IOException e) {
            handleException(e);
            return;
        }

        try {
            osConsumer.accept(writer);
        } finally {
            writer.flush();
        }
    }

    private boolean trySendContent(ServletOutputStream output, BElement body) throws Exception {
        if (output instanceof HttpOutput) {
            HttpOutput httpOutput = (HttpOutput) output;
            if (body.isReference()) {
                var ref = body.asReference().getReference();
                if (ref instanceof File) {
                    File file = (File) ref;
                    if (mmapEnabled) {
                        long length = file.length();
                        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                            ByteBuffer buffer = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0, length);
                            httpOutput.sendContent(buffer);
                        }
                    } else {
                        try (InputStream input = new FileInputStream(file)) {
                            httpOutput.sendContent(input);
                        }
                    }
                } else if (ref instanceof ByteBuffer) {
                    httpOutput.sendContent((ByteBuffer) ref);
                } else if (ref instanceof InputStream) {
                    httpOutput.sendContent((InputStream) ref);
                } else if (ref instanceof ReadableByteChannel) {
                    httpOutput.sendContent((ReadableByteChannel) ref);
                } else {
                    return false;
                }

                return true;
            }
        }
        return false;
    }

    protected void writeBodyBinary(BElement body, HttpServletResponse response, LongConsumer contentLengthConsumer) {
        takeOutputStream(response, output -> {
            try {
                if (!trySendContent(output, body)) {
                    body.writeBytes(output, format);
                }
            } catch (Exception e) {
                handleException(e);
            }
        });
    }

    protected void writeBodyBinary(BElement body, HttpServletResponse response) {
        writeBodyBinary(body, response, null);
    }

    protected void writeBodyJson(BElement body, HttpServletResponse response) {
        if (body instanceof BValue) {
            writeBodyTextPlain(body, response);
        } else if (body instanceof BReference) {
            writeBodyBinary(body, response);
        } else {
            takeOutputStream(response, body::writeJson);
        }
    }

    protected void writeBodyMultipart(@NonNull BElement body, @NonNull HttpServletResponse response, @NonNull Consumer<String> contentTypeConsumer) {
        var builder = MultipartEntityBuilder.create();
        if (body instanceof BObject) {
            for (var entry : body.asObject().entrySet()) {
                String name = entry.getKey();
                addMultipartEntity(name, entry.getValue(), builder);
            }
        } else if (body instanceof BArray) {
            for (var entry : body.asArray()) {
                addMultipartEntity(null, entry, builder);
            }
        } else {
            addMultipartEntity(null, body, builder);
        }

        takeOutputStream(response, outstream -> {
            try {
                var entity = builder.build();
                contentTypeConsumer.accept(entity.getContentType().getValue());
                entity.writeTo(outstream);
            } catch (IOException e) {
                handleException(new RuntimeIOException(e));
            }
        });

    }

    protected void addMultipartEntity(String name, @NonNull BElement value, @NonNull MultipartEntityBuilder builder) {
        name = name == null ? "" : name;
        if (value.isValue()) {
            if (value.getType() == BType.RAW) {
                builder.addBinaryBody(name, value.asValue().getRaw());
            } else {
                builder.addTextBody(name, value.asValue().getString());
            }
        } else if (value.isReference()) {
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
        } else {
            builder.addPart(name, new StringBody(value.toJson(), ContentType.APPLICATION_JSON));
        }
    }

    protected void writeBodyTextPlain(BElement body, HttpServletResponse response) {
        if (body instanceof BReference) {
            writeBodyBinary(body, response, //
                    contentLength -> response.addHeader(HttpCommonConstants.CONTENT_LENGTH, String.valueOf(contentLength)));
        } else {
            if (body.isValue()) {
                if (body.getType() == BType.RAW) {
                    takeOutputStream(response, output -> {
                        try {
                            output.write(body.asValue().getRaw());
                        } catch (IOException e) {
                            handleException(e);
                        }
                    });
                } else {
                    takeWriter(response, writer -> writer.write(body.asValue().getString()));
                }
            } else {
                takeOutputStream(response, body::writeJson);
            }
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

    @Override
    public void writeResponse(HttpServletResponse response, Message message) {
        /* -------------------------------------- */
        /**
         * process header
         */
        var headers = message.headers();
        var body = message.body();

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
}
