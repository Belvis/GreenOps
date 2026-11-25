
package com.greenops.webaudit.service;

import com.greenops.webaudit.dto.DomAnalysisResult;
import com.greenops.webaudit.dto.WebAuditResult;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JsoupAnalysisServiceTest {

    private WebPageFetcher webPageFetcher;
    private PageSpeedService pageSpeedService;
    private LighthouseExtractor lighthouseExtractor;
    private EcoIndexCalculator ecoIndexCalculator;
    private JsoupAnalysisService service;

    @BeforeEach
    void setUp() {
        webPageFetcher = mock(WebPageFetcher.class);
        pageSpeedService = mock(PageSpeedService.class);
        lighthouseExtractor = mock(LighthouseExtractor.class);
        ecoIndexCalculator = mock(EcoIndexCalculator.class);

        // Simule une indisponibilité de Google PageSpeed.
        lenient().when(pageSpeedService.analyze(anyString()))
                .thenThrow(
                        new IllegalStateException("PageSpeed indisponible")
                );

        service = new JsoupAnalysisService(
                webPageFetcher,
                pageSpeedService,
                lighthouseExtractor,
                ecoIndexCalculator
        );
    }

    @Test
    void shouldAnalyzeHtmlPage() throws IOException {
        String html = """
                <html>
                    <head><title>GreenOps Test</title></head>
                    <body>
                        <h1>Bienvenue</h1>
                        <img src="image.jpg">
                        <img src="logo.png" alt="Logo">
                        <link rel="stylesheet" href="style.css">
                        <script src="app.js"></script>
                    </body>
                </html>
                """;

        WebPageFetcher.FetchedPage page =
                new WebPageFetcher.FetchedPage(
                        200,
                        Jsoup.parse(html),
                        html.getBytes().length
                );

        when(webPageFetcher.fetch(any(URI.class))).thenReturn(page);

        WebAuditResult result =
                service.analyze("https://example.com");

        DomAnalysisResult dom = result.dom();

        assertEquals("https://example.com", dom.url());
        assertEquals("GreenOps Test", dom.title());
        assertEquals(2, dom.totalImages());
        assertEquals(1, dom.imagesWithoutAlt());
        assertEquals(1, dom.cssFiles());
        assertEquals(1, dom.scripts());
        assertEquals(html.getBytes().length, dom.htmlSizeBytes());

        assertNull(result.lighthouse());
        assertNull(result.ecoIndex());
        assertEquals("UNAVAILABLE", result.lighthouseStatus());
        assertEquals(
                "PageSpeed indisponible",
                result.lighthouseError()
        );

        verify(webPageFetcher).fetch(any(URI.class));
        verify(pageSpeedService).analyze("https://example.com");
    }

    @Test
    void shouldRejectBlankUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze(" ")
        );

        verifyNoInteractions(webPageFetcher);
        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldRejectInvalidUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze("not-a-url")
        );

        verifyNoInteractions(webPageFetcher);
        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldRejectNonHttpUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze("ftp://example.com")
        );

        verifyNoInteractions(webPageFetcher);
        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldRejectUrlWithUserInfo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze(
                        "https://user:password@example.com"
                )
        );

        verifyNoInteractions(webPageFetcher);
        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldRejectPrivateAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyze("http://127.0.0.1")
        );

        verifyNoInteractions(webPageFetcher);
        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldRejectNonSuccessfulHttpStatus() throws IOException {
        WebPageFetcher.FetchedPage page =
                new WebPageFetcher.FetchedPage(
                        404,
                        Jsoup.parse("<html></html>"),
                        0
                );

        when(webPageFetcher.fetch(any(URI.class))).thenReturn(page);

        assertThrows(
                IllegalStateException.class,
                () -> service.analyze("https://example.com")
        );

        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldHandleFetchIOException() throws IOException {
        when(webPageFetcher.fetch(any(URI.class)))
                .thenThrow(new IOException("Connexion impossible"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.analyze("https://example.com")
        );

        assertTrue(
                exception.getMessage().contains(
                        "Impossible d'analyser le site"
                )
        );

        verifyNoInteractions(pageSpeedService);
    }

    @Test
    void shouldCountAllDomElements() throws IOException {
        String html = """
                <html>
                    <head><title>Test</title></head>
                    <body>
                        <main>
                            <section>
                                <p>Contenu</p>
                            </section>
                        </main>
                    </body>
                </html>
                """;

        WebPageFetcher.FetchedPage page =
                new WebPageFetcher.FetchedPage(
                        200,
                        Jsoup.parse(html),
                        html.getBytes().length
                );

        when(webPageFetcher.fetch(any(URI.class))).thenReturn(page);

        WebAuditResult result =
                service.analyze("https://example.com");

        assertTrue(result.dom().totalElements() > 0);
        assertEquals("UNAVAILABLE", result.lighthouseStatus());
        assertNull(result.ecoIndex());
    }
}
