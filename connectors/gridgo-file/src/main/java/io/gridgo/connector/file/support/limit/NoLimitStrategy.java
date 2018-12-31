package io.gridgo.connector.file.support.limit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import lombok.Getter;

public class NoLimitStrategy extends AbstractFileLimitStrategy {

    private String path;

    private String mode;

    private boolean deleteOnStartup;

    private boolean deleteOnShutdown;

    @Getter
    private FileChannel fileChannel;

    private File file;

    private RandomAccessFile raf;

    private boolean override;

    public NoLimitStrategy(String path, String mode, boolean deleteOnStartup, boolean deleteOnShutdown, boolean override) {
        this.path = path;
        this.mode = mode;
        this.deleteOnStartup = deleteOnStartup;
        this.deleteOnShutdown = deleteOnShutdown;
        this.override = override;
    }

    @Override
    public void putBytes(long bytes) throws IOException {
        // Nothing to do here
    }

    @Override
    public void readWith(RandomAccessFileHandler consumer) throws IOException {
        if (!this.file.exists())
            return;
        try (var raf = new RandomAccessFile(this.file, "r")) {
            consumer.process(raf);
        }
    }

    @Override
    public void start() throws IOException {
        this.file = new File(path);
        if (deleteOnStartup)
            deleteFile(file);
        this.raf = new RandomAccessFile(file, mode);
        this.fileChannel = raf.getChannel();
        if (!override)
            this.raf.seek(this.raf.length());
    }

    @Override
    public void stop() throws IOException {
        this.raf.close();
        if (deleteOnShutdown)
            deleteFile(file);
    }
}
