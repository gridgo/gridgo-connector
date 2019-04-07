package io.gridgo.socket.netty4.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.socket.netty4.exceptions.SSLContextException;
import io.gridgo.utils.exception.RuntimeIOException;
import lombok.Getter;
import lombok.NonNull;

public class SSLContextRegistry {

    private final Map<String, SSLContext> contexts = new NonBlockingHashMap<>();

    @Getter
    private static final SSLContextRegistry instance = new SSLContextRegistry();

    private SSLContextRegistry() {
        // private constructor
    }

    public void register(@NonNull String name //
            , @NonNull File keyStoreFile //
            , @NonNull KeyStoreType type //
            , @NonNull String algorithm //
            , @NonNull String protocol//
            , String password) {
        try (InputStream input = new FileInputStream(keyStoreFile)) {
            this.register(name, keyStoreFile, type, algorithm, protocol, password);
        } catch (FileNotFoundException e) {
            throw new SSLContextException("File not found: " + keyStoreFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeIOException("Error while reading file", e);
        }
    }

    public void register(@NonNull String name //
            , @NonNull String keyStoreContent //
            , @NonNull KeyStoreType type //
            , @NonNull String algorithm //
            , @NonNull String protocol //
            , String password) {
        this.register(name, keyStoreContent.getBytes(), type, algorithm, protocol, password);
    }

    public void register(@NonNull String name //
            , @NonNull byte[] keyStoreContent //
            , @NonNull KeyStoreType type //
            , @NonNull String algorithm //
            , @NonNull String protocol //
            , String password) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(keyStoreContent)) {
            this.register(name, inputStream, type, algorithm, protocol, password);
        } catch (IOException e) {
            throw new RuntimeIOException("Error while reading keyStoreContent", e);
        }
    }

    public void register(@NonNull String name //
            , @NonNull InputStream keyStoreInputStream //
            , @NonNull KeyStoreType type //
            , @NonNull String algorithm //
            , @NonNull String protocol //
            , String password) {
        try {
            final KeyStore keyStore = KeyStore.getInstance(type.name().toLowerCase());
            final char[] keyStorePassword = (password == null ? "" : password).toCharArray();
            keyStore.load(keyStoreInputStream, keyStorePassword);

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(keyStore, keyStorePassword);

            final SSLContext context = SSLContext.getInstance(protocol);
            context.init(kmf.getKeyManagers(), null, null);

            this.register(name, context);
        } catch (Exception e) {
            throw new SSLContextException("Key store exception", e);
        }
    }

    public void register(@NonNull String name, @NonNull SSLContext context) {
        var old = this.contexts.putIfAbsent(name, context);
        if (old != null) {
            throw new SSLContextException("SSLContext instance for name " + name + " is already registered");
        }
    }

    public SSLContext lookup(@NonNull String name) {
        return this.contexts.get(name);
    }

    public SSLContext lookupMandatory(@NonNull String name) {
        var result = this.lookup(name);
        if (result == null) {
            throw new SSLContextException("SSLContext instance cannot be found for name: " + name);
        }
        return result;
    }
}
