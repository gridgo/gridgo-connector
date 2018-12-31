package io.gridgo.connector.file.support.engines;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.gridgo.connector.file.FileConsumer;
import io.gridgo.connector.file.support.exceptions.FileReadException;
import io.gridgo.connector.file.support.exceptions.LengthMismatchException;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.MultipartMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LengthPrependedFileConsumerEngine implements FileConsumerEngine {

    private FileConsumer fileConsumer;

    public LengthPrependedFileConsumerEngine(FileConsumer fileConsumer) {
        this.fileConsumer = fileConsumer;
    }

    @Override
    public String getFormat() {
        return this.fileConsumer.getFormat();
    }

    @Override
    public void readAndPublish() {
        var buffer = this.fileConsumer.getBuffer();
        try {
            this.fileConsumer.getLimitStrategy() //
                             .readWith(raf -> readAndPublish(buffer, raf));
        } catch (IOException ex) {
            log.error("Exception caught when processing file", ex);
            throw new FileReadException(ex);
        }
    }

    private void readAndPublish(byte[] buffer, RandomAccessFile randomAccessFile) throws IOException {
        while (true) {
            var length = tryGetLength(randomAccessFile);
            if (length == -1)
                break;
            if (length > buffer.length) {
                log.warn("Buffer overflow detected. Limit: %d. Required: %d", buffer.length, length);
                buffer = new byte[length];
            }
            int read = randomAccessFile.read(buffer, 0, length);
            if (read != length)
                throw new LengthMismatchException(length, read);
            var payload = deserialize(buffer, length);
            var msg = Message.parse(payload);
            if (msg instanceof MultipartMessage) {
                var messages = ((MultipartMessage) msg).buildOriginalMessages();
                for (var message : messages) {
                    this.fileConsumer.publishMessage(message);
                }
            } else {
                this.fileConsumer.publishMessage(msg);
            }
        }
    }

    private int tryGetLength(RandomAccessFile randomAccessFile) throws IOException {
        try {
            return randomAccessFile.readInt();
        } catch (EOFException ex) {
            return -1;
        }
    }
}
