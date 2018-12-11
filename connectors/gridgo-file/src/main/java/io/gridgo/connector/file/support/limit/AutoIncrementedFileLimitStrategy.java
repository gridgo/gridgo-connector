package io.gridgo.connector.file.support.limit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import lombok.Getter;

public class AutoIncrementedFileLimitStrategy implements FileLimitStrategy {

    private String basePath;

    private String mode;

    private long limit;

    private RandomAccessFile file;

    @Getter
    private FileChannel fileChannel;

    private long written = -1;

    private LinkedList<File> files;

    private boolean deleteOnStartup;

    private boolean deleteOnShutdown;

    private boolean override;

    public AutoIncrementedFileLimitStrategy(String basePath, String mode, long limit, boolean deleteOnStartup,
            boolean deleteOnShutdown, boolean override) throws IOException {
        this.basePath = basePath;
        this.mode = mode;
        this.limit = limit;
        this.deleteOnStartup = deleteOnStartup;
        this.deleteOnShutdown = deleteOnShutdown;
        this.override = override;
    }

    @Override
    public void start() throws IOException {
        this.files = initFiles();
        if (deleteOnStartup) {
            deleteFiles();
            this.files = initFiles();
        }
        resetFile();
    }

    @Override
    public void stop() throws IOException {
        closeFile();
        if (deleteOnShutdown)
            deleteFiles();
    }

    private LinkedList<File> initFiles() {
        var files = new LinkedList<File>();
        files.add(new File(basePath));
        for (var i = 0;; i++) {
            var file = new File(basePath + "." + i);
            if (!file.exists())
                break;
            files.add(file);
        }
        return files;
    }

    private void deleteFiles() {
        for (var file : files) {
            file.delete();
        }
    }

    @Override
    public void putBytes(long bytes) throws IOException {
        this.written += bytes;
        if (this.limit > 0 && this.written > this.limit) {
            increment();
        }
    }

    private void increment() throws IOException {
        closeFile();
        var last = this.files.size();
        var file = new File(basePath + "." + last);
        file.delete();
        this.files.add(file);
        resetFile();
    }

    private void closeFile() throws IOException {
        this.file.close();
    }

    private void resetFile() throws IOException {
        this.file = new RandomAccessFile(files.getLast(), mode);
        if (!override) {
            var length = this.file.length();
            this.file.seek(length);
            this.written = length;
        } else {
            this.written = 0;
        }
        this.fileChannel = this.file.getChannel();
    }

    @Override
    public void readWith(RandomAccessFileHandler consumer) throws IOException {
        for (var file : files) {
            try (var raf = new RandomAccessFile(file, "r")) {
                consumer.process(raf);
            }
        }
    }
}
