package io.gridgo.jetty.test.echo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import io.gridgo.utils.ThreadUtils;

public class EchoHttpClient {

    private static final int N_THREADS = 8;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: provide argument 1 as <endpoint>, example: http://localhost:8080/path");
            return;
        }
        final var app = new EchoHttpClient(args[0]);
        ThreadUtils.registerShutdownTask(() -> {
            app.stop();
        });
        app.start();
    }

    private final HttpClient httpClient;
    private final String endpoint;

    private EchoHttpClient(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = HttpClient.newBuilder() //
                                    .executor(Executors.newFixedThreadPool(N_THREADS)) //
                                    .followRedirects(Redirect.ALWAYS) //
                                    .build();
    }

    private HttpRequest buildRequest(int id) {
        URI uri = URI.create(this.endpoint + "?index=" + id);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        return request;
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest request) {
        return httpClient.sendAsync(request, BodyHandlers.ofString());
    }

    public void start() {
        int idSeed = 0;
        while (!ThreadUtils.isShuttingDown()) {
            for (int i = 0; i < N_THREADS; i++) {
                final int id = idSeed++;
                this.sendRequest(buildRequest(id));
            }
            ThreadUtils.sleep(100);
        }
    }

    public void stop() {
        // do nothing
    }
}
