package io.gridgo.connector.file.support.limit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import lombok.Getter;

public class AutoIncrementedFileLimitStrategy extends AbstractFileLimitStrategy {

    private String basePath;

    private String mode;

    private long limit;

    private RandomAccessFile raf;

    @Getter
    private FileChannel fileChannel;

    private long written = -1;

    private LinkedList<File> fileList;

    private boolean deleteOnStartup;

    private boolean deleteOnShutdown;

    private boolean override;

    public AutoIncrementedFileLimitStrategy(String basePath, String mode, long limit, boolean deleteOnStartup, boolean deleteOnShutdown, boolean override) {
        this.basePath = basePath;
        this.mode = mode;
        this.limit = limit;
        this.deleteOnStartup = deleteOnStartup;
        this.deleteOnShutdown = deleteOnShutdown;
        this.override = override;
    }

    private void closeFile() throws IOException {
        this.raf.close();
    }

    private void deleteFiles() {
        for (var file : fileList) {
            deleteFile(file);
        }
    }

    private void increment() throws IOException {
        closeFile();
        var last = this.fileList.size();
        var file = new File(basePath + "." + last);
        deleteFile(file);
        this.fileList.add(file);
        resetFile();
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

    @Override
    public void putBytes(long bytes) throws IOException {
        this.written += bytes;
        if (this.limit > 0 && this.written > this.limit) {
            increment();
        }
    }

    @Override
    public void readWith(RandomAccessFileHandler consumer) throws IOException {
        for (var file : fileList) {
            try (var theRaf = new RandomAccessFile(file, "r")) {
                consumer.process(theRaf);
            }
        }
    }

    private void resetFile() throws IOException {
        this.raf = new RandomAccessFile(fileList.getLast(), mode);
        if (!override) {
            var length = this.raf.length();
            this.raf.seek(length);
            this.written = length;
        } else {
            this.written = 0;
        }
        this.fileChannel = this.raf.getChannel();
    }

    @Override
    public void start() throws IOException {
        this.fileList = initFiles();
        if (deleteOnStartup) {
            deleteFiles();
            this.fileList = initFiles();
        }
        resetFile();
    }

    @Override
    public void stop() throws IOException {
        closeFile();
        if (deleteOnShutdown)
            deleteFiles();
    }
}
