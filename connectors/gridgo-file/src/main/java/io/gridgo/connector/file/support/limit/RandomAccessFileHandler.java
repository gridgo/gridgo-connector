package io.gridgo.connector.file.support.limit;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface RandomAccessFileHandler {

    public void process(RandomAccessFile raf) throws IOException;
}
