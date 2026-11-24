
package com.greenops.webaudit.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WebPageFetcherTest {

    private WebPageFetcher webPageFetcher;
    private HttpServer server;
    private URI serverUri;

    @BeforeEach
    void setUp() throws Exception {
        webPageFetcher = new WebPageFetcher();

        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        server.createContext("/", exchange -> {
            String html = """
                    <!DOCTYPE html>
                    <html>
                        <head>
                            <title>GreenOps Test</title>
                        </head>
                        <body>
                            <h1>Bienvenue sur GreenOps</h1>
                        </body>
                    </html>
                    """;

            byte[] response = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        server.start();

        serverUri = URI.create(
                "http://127.0.0.1:" +
                server.getAddress().getPort() +
                "/"
        );
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFetchHtmlPageSuccessfully() throws Exception {

        WebPageFetcher.FetchedPage result =
                webPageFetcher.fetch(serverUri);

        assertNotNull(result);
        assertEquals(200, result.statusCode());
        assertNotNull(result.document());
        assertEquals("GreenOps Test", result.document().title());
        assertTrue(result.bodySizeBytes() > 0);
    }

    @Test
    void shouldReturnCorrectPageContent() throws Exception {

        WebPageFetcher.FetchedPage result =
                webPageFetcher.fetch(serverUri);

        assertTrue(
                result.document()
                        .html()
                        .contains("Bienvenue sur GreenOps")
        );
    }

    @Test
    void shouldReturnCorrectBodySize() throws Exception {

        WebPageFetcher.FetchedPage result =
                webPageFetcher.fetch(serverUri);

        assertTrue(result.bodySizeBytes() > 0);

        assertEquals(
                result.bodySizeBytes(),
                result.bodySizeBytes()
        );
    }
}
