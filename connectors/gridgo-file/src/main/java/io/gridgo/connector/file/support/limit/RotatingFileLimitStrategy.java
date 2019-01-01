package io.gridgo.connector.file.support.limit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RotatingFileLimitStrategy extends AbstractFileLimitStrategy {

    private String basePath;

    private String mode;

    private long limit;

    private int count;

    private RandomAccessFile file;

    @Getter
    private FileChannel fileChannel;

    private long written = -1;

    private File[] files;

    private boolean deleteOnStartup;

    private boolean deleteOnShutdown;

    private boolean override;

    public RotatingFileLimitStrategy(String basePath, String mode, long limit, int count, boolean deleteOnStartup,
            boolean deleteOnShutdown, boolean override) throws IOException {
        this.basePath = basePath;
        this.mode = mode;
        this.limit = limit;
        this.count = count;
        this.deleteOnStartup = deleteOnStartup;
        this.deleteOnShutdown = deleteOnShutdown;
        this.override = override;
        this.files = initFiles();
    }

    private void closeFile() throws IOException {
        this.file.close();
    }

    private void deleteFiles() {
        for (var file : files) {
            deleteFile(file);
        }
    }

    private File[] initFiles() {
        var files = new File[count];
        files[0] = new File(basePath);
        for (int i = 1; i < count; i++) {
            files[i] = new File(basePath + "." + (i - 1));
        }
        return files;
    }

    @Override
    public void putBytes(long bytes) throws IOException {
        this.written += bytes;
        if (this.limit > 0 && this.written > this.limit) {
            rotate();
        }
    }

    @Override
    public void readWith(RandomAccessFileHandler consumer) throws IOException {
        for (int i = files.length - 1; i >= 0; i--) {
            if (!files[i].exists())
                continue;
            try (var raf = new RandomAccessFile(files[i], "r")) {
                consumer.process(raf);
            }
        }
    }

    private void resetFile() throws IOException {
        this.file = new RandomAccessFile(basePath, mode);
        if (!override) {
            var length = this.file.length();
            this.file.seek(length);
            this.written = length;
        } else {
            this.written = 0;
        }
        this.fileChannel = this.file.getChannel();
    }

    private void rotate() throws IOException {
        closeFile();
        for (int i = count - 2; i >= 0; i--) {
            File f1 = files[i];
            File f2 = files[i + 1];
            if (!f1.exists())
                continue;
            if (f2.exists())
                deleteFile(f2);
            tryRename(f1, f2);
        }
        resetFile();
    }

    private void tryRename(File f1, File f2) {
        if (f1.renameTo(f2))
            return;
        if (log.isWarnEnabled())
            log.warn("Cannot rename file from {} to {}", f1.getAbsolutePath(), f2.getAbsolutePath());
    }

    @Override
    public void start() throws IOException {
        if (deleteOnStartup)
            deleteFiles();
        resetFile();
    }

    @Override
    public void stop() throws IOException {
        closeFile();
        if (deleteOnShutdown)
            deleteFiles();
    }
}
