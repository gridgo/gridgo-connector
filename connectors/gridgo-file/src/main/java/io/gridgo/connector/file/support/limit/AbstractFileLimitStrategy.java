package io.gridgo.connector.file.support.limit;

import java.io.File;

import io.gridgo.utils.helper.Loggable;

public abstract class AbstractFileLimitStrategy implements FileLimitStrategy, Loggable {

    protected void deleteFile(File file) {
        if (file.delete())
            return;
        if (getLogger().isWarnEnabled())
            getLogger().warn("Failed to delete file {}", file.getAbsolutePath());
    }
}
