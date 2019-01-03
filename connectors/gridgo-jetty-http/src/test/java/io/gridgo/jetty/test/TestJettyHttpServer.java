package io.gridgo.jetty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;

public class TestJettyHttpServer {

    private static final String TEST_TEXT = "this is test text";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final JettyHttpServerManager serverManager = JettyHttpServerManager.getInstance();
    private final String address = "localhost:8888";

    @Test
    public void testAllInterfaceThenLocalhost() {
        System.out.println("Test create server binding on all interface (0.0.0.0) then localhost");

        JettyHttpServer httpServer1 = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
        JettyHttpServer httpServer2 = serverManager.getOrCreateJettyServer("localhost:8889", true);
        JettyHttpServer httpServer3 = serverManager.getOrCreateJettyServer("*:8889", true);

        assertTrue(httpServer1 == httpServer2);
        assertTrue(httpServer1 == httpServer3);

        // start then stop to remove current http server from manager, make sure other
        // test case run properly
        httpServer1.start();
        httpServer1.stop();

        System.out.println("*** DONE ***");
    }

    @Test
    public void testLocalhostThenAllInterface() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Test create server binding on localhost then all interface (0.0.0.0)");

        String osName = System.getProperty("os.name");
        System.out.println("os name: " + osName);

        JettyHttpServer localhostServer = serverManager.getOrCreateJettyServer("localhost:8889", true);
        localhostServer.start();

        final AtomicReference<String> localhostReceived = new AtomicReference<String>(null);
        localhostServer.addPathHandler("/*", (req, res) -> {
            localhostReceived.set(req.getParameter("key"));
        });

        AtomicReference<Exception> errorRef = new AtomicReference<Exception>(null);
        JettyHttpServer allInterfaceServer = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
        try {
            allInterfaceServer.start();
        } catch (Exception e) {
            errorRef.set(e);
        }

        if (osName.equalsIgnoreCase("Mac OS X")) {
            final AtomicReference<String> allInterfaceReceived = new AtomicReference<String>(null);
            allInterfaceServer.addPathHandler("/*", (req, res) -> {
                allInterfaceReceived.set(req.getParameter("key"));
            });

            final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
            URI uri = new URI("http://localhost:8889/?key=" + encodedText);
            HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
            httpClient.send(request, BodyHandlers.ofString());

            assertNull(allInterfaceReceived.get());
            assertEquals(TEST_TEXT, localhostReceived.get());
            allInterfaceServer.stop();
        } else {
            assertNotNull(errorRef.get());
        }

        localhostServer.stop();
        System.out.println("*** DONE ***");
    }

    @Test
    public void testMultiPath() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Test handling on multi path");
        JettyHttpServer httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        final CountDownLatch doneSignal = new CountDownLatch(3);

        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>(null);

        BiConsumer<HttpServletRequest, HttpServletResponse> handler = (req, res) -> {
            try {
                res.getWriter().write("");
                res.getWriter().flush();
            } catch (IOException e) {
                exception.set(e);
            } finally {
                doneSignal.countDown();
            }
        };

        httpServer.addPathHandler("/*", handler) //
                  .addPathHandler("/path1/*", handler) //
                  .addPathHandler("/path2/*", handler);

        final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());

        URI uri = new URI("http://" + address + "/path1/subpath?key=" + encodedText);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        uri = new URI("http://" + address + "/path2/subpath?key=" + encodedText);
        request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        uri = new URI("http://" + address + "/other-path?key=" + encodedText);
        request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        doneSignal.await(5, TimeUnit.SECONDS);

        assertNull(exception.get());

        httpServer.stop();
        System.out.println("*** DONE ***");
    }

    @Test
    public void testPingPong() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Test ping pong");
        JettyHttpServer httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        final CountDownLatch doneSignal = new CountDownLatch(1);
        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>(null);

        httpServer.addPathHandler("/*", (req, res) -> {
            String value = req.getParameter("key");
            try {
                res.getWriter().write(value);
                res.getWriter().flush();
            } catch (IOException e) {
                exception.set(e);
                doneSignal.countDown();
            }
        });

        final AtomicReference<String> responseText = new AtomicReference<>(null);

        String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
        URI uri = new URI("http://" + address + "/?key=" + encodedText);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        HttpClient httpClient = HttpClient.newHttpClient();
        httpClient.sendAsync(request, BodyHandlers.ofString()).thenAccept((res) -> {
            try {
                responseText.set((String) res.body());
            } finally {
                doneSignal.countDown();
            }
        });

        doneSignal.await(5, TimeUnit.SECONDS);

        assertNull(exception.get());
        assertEquals(TEST_TEXT, responseText.get());

        httpServer.stop();
        System.out.println("*** DONE ***");
    }
}
