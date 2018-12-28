package org.nanomsg;

import io.gridgo.socket.helper.AbstractEmbeddedLibraryTools;

public class EmbeddedLibraryTools extends AbstractEmbeddedLibraryTools {

    public static final boolean LOADED_EMBEDDED_LIBRARY;

    static {
        LOADED_EMBEDDED_LIBRARY = new EmbeddedLibraryTools().loadEmbeddedLibrary();
    }

    private EmbeddedLibraryTools() {
        // Nothing to do
    }

    @Override
    protected String[] getPossibleLibs() {
        final var libsFromProps = System.getProperty("jnano.libs");
        if (libsFromProps != null)
            return libsFromProps.split(",");
        return new String[] { "libnanomsg", "nanomsg", "libjnano", "jnano" };
    }
}
