package io.gridgo.socket;

public interface PollingReceiver<PayloadType> {

    default int receive(PayloadType buffer) {
        return this.receive(buffer, true);
    }

    int receive(PayloadType buffer, boolean block);
}
