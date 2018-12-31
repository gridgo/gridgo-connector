package io.gridgo.socket.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEmbeddedLibraryTools {

    protected void catalogArchive(final File jarfile, final Collection<String> files) {
        try (var j = new JarFile(jarfile)) {
            final var e = j.entries();
            while (e.hasMoreElements()) {
                final JarEntry entry = e.nextElement();
                if (!entry.isDirectory()) {
                    files.add(entry.getName());
                }
            }
        } catch (IOException ex) {
            log.error("Exception caught while opening JAR file", ex);
        }
    }

    protected Collection<String> catalogClasspath() {

        final var files = new ArrayList<String>();
        final var classpath = System.getProperty("java.class.path", "").split(File.pathSeparator);

        for (final String path : classpath) {
            final File tmp = new File(path);
            if (tmp.isFile() && path.toLowerCase().endsWith(".jar")) {
                catalogArchive(tmp, files);
            } else if (tmp.isDirectory()) {
                final int len = tmp.getPath().length() + 1;
                catalogFiles(len, tmp, files);
            }
        }

        return files;

    }

    protected void catalogFiles(final int prefixlen, final File root, final Collection<String> files) {
        final var ff = root.listFiles();
        if (ff == null) {
            throw new IllegalStateException("invalid path listed: " + root);
        }

        for (final File f : ff) {
            if (f.isDirectory()) {
                catalogFiles(prefixlen, f, files);
            } else {
                files.add(f.getPath().substring(prefixlen));
            }
        }
    }

    protected URL findNativeLibrary(String[] allowedExtensions, StringBuilder url, String lib) {
        // loop through extensions, stopping after finding first one
        for (String ext : allowedExtensions) {
            var nativeLibraryUrl = AbstractEmbeddedLibraryTools.class.getResource(url.toString() + lib + "." + ext);
            if (nativeLibraryUrl != null)
                return nativeLibraryUrl;
        }
        return null;
    }

    protected String getCurrentPlatformIdentifier() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows")) {
            osName = "Windows";
        } else if (osName.toLowerCase().contains("mac os x")) {
            osName = "Darwin";
        } else {
            osName = osName.replaceAll("\\s+", "_");
        }
        return System.getProperty("os.arch") + "/" + osName;
    }

    protected Collection<String> getEmbeddedLibraryList() {
        final var result = new ArrayList<String>();
        final var files = catalogClasspath();

        for (final String file : files) {
            if (file.startsWith("NATIVE")) {
                result.add(file);
            }
        }

        return result;
    }

    protected String[] getPossibleLibs() {
        return new String[0];
    }

    public boolean loadEmbeddedLibrary() {

        boolean usingEmbedded = false;

        // attempt to locate embedded native library within JAR at following location:
        // /NATIVE/${os.arch}/${os.name}/libname.[so|dylib|dll]
        String[] allowedExtensions = new String[] { "so", "dylib", "dll" };
        var libs = getPossibleLibs();
        StringBuilder url = new StringBuilder();
        url.append("/NATIVE/");
        url.append(getCurrentPlatformIdentifier()).append("/");

        log.info("Trying to load embedded lib from: " + url);

        for (String lib : libs) {
            URL nativeLibraryUrl = findNativeLibrary(allowedExtensions, url, lib);

            if (nativeLibraryUrl == null)
                continue;
            // native library found within JAR, extract and load
            try {

                final File libfile = File.createTempFile(lib, ".lib");
                libfile.deleteOnExit(); // just in case

                try (var in = nativeLibraryUrl.openStream(); var out = new BufferedOutputStream(new FileOutputStream(libfile))) {

                    int len = 0;
                    byte[] buffer = new byte[8192];
                    while ((len = in.read(buffer)) > -1)
                        out.write(buffer, 0, len);
                }
                System.load(libfile.getAbsolutePath());

                usingEmbedded = true;

                log.info("Embedded lib {} loaded from: {}", lib, nativeLibraryUrl);
            } catch (IOException ex) {
                log.warn("Exception caught while loading native library", ex);
            }
        }
        return usingEmbedded;
    }
}
