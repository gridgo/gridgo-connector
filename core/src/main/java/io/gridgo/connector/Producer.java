package io.gridgo.connector;

import org.joo.promise4j.Promise;

import io.gridgo.connector.support.ProducerCapability;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import lombok.NonNull;

/**
 * Represents a message producer. Producers are used for sending messages to the
 * endpoint.
 */
public interface Producer extends ComponentLifecycle, ProducerCapability {

    /**
     * Send a message to the endpoint. This method does not guarantee the message to
     * be sent successfully. There is no exception thrown or any way to check the
     * status of the sending. This is called fire-and-forget communication.
     * 
     * @param message the message to be sent
     */
    public void send(final @NonNull Message message);

    /**
     * Send a message to the endpoint. This method is similar to <code>send()</code>
     * but will return a <code>Promise</code> to let the callers know whether the
     * sending has been successful or not. Note that even in case it is successful
     * the result of the promise will always be <code>null</code>.
     * 
     * @param message the message to be sent
     * @return the promise of the sending status
     */
    public Promise<Message, Exception> sendWithAck(final @NonNull Message message);

    /**
     * Send a message to the endpoint and get the response. This method will return
     * a <code>Promise</code> to let the callers know whether the sending has been
     * successful or not. In case it is successful, the result of the promise will
     * be the response. This is called RPC communication. Note that it might not be
     * supported by some producers.
     * 
     * @param request the request to be sent
     * @return the promise of the sending, containing either the response (if the
     *         sending was successful) or the exception (if the sending failed)
     */
    public Promise<Message, Exception> call(final @NonNull Message request);
}
