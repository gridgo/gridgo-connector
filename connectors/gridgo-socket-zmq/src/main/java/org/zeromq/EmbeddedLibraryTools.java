package org.zeromq;

import io.gridgo.socket.helper.AbstractEmbeddedLibraryTools;

public class EmbeddedLibraryTools extends AbstractEmbeddedLibraryTools {

    public static final boolean LOADED_EMBEDDED_LIBRARY;

    static {
        LOADED_EMBEDDED_LIBRARY = loadEmbeddedLibrary();
    }

    private EmbeddedLibraryTools() {
    }

    protected static String[] getPossibleLibs() {
        final String libsFromProps = System.getProperty("jzmq.libs");
        if (libsFromProps != null)
            return libsFromProps.split(",");
        return new String[] { "libsodium", "libpgm", "libzmq", "libjzmq" };
    }
}
