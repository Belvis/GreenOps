
package com.greenops.webaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.greenops.webaudit.dto.DomAnalysisResult;
import com.greenops.webaudit.dto.WebAuditResult;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class JsoupAnalysisService {

    private final WebPageFetcher webPageFetcher;
    private final PageSpeedService pageSpeedService;
    private final LighthouseExtractor lighthouseExtractor;
    private final EcoIndexCalculator ecoIndexCalculator;

    public JsoupAnalysisService(
            WebPageFetcher webPageFetcher,
            PageSpeedService pageSpeedService,
            LighthouseExtractor lighthouseExtractor,
            EcoIndexCalculator ecoIndexCalculator
    ) {
        this.webPageFetcher = webPageFetcher;
        this.pageSpeedService = pageSpeedService;
        this.lighthouseExtractor = lighthouseExtractor;
        this.ecoIndexCalculator = ecoIndexCalculator;
    }

    public WebAuditResult analyze(String rawUrl) {

        URI uri = validateUrl(rawUrl);

        DomAnalysisResult dom;

        // 1. Analyse DOM
        try {
            WebPageFetcher.FetchedPage page =
                    webPageFetcher.fetch(uri);

            if (page.statusCode() < 200 || page.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Le site a retourné le statut HTTP "
                                + page.statusCode()
                );
            }

            Document document = page.document();

            int totalElements = document.getAllElements().size();
            int totalImages = document.select("img").size();

            int imagesWithoutAlt =
                    document.select("img:not([alt])").size();

            int cssFiles =
                    document.select("link[rel=stylesheet]").size();

            int scripts = document.select("script").size();

            dom = new DomAnalysisResult(
                    uri.toString(),
                    document.title(),
                    totalElements,
                    totalImages,
                    imagesWithoutAlt,
                    cssFiles,
                    scripts,
                    page.bodySizeBytes()
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible d'analyser le site : "
                            + exception.getMessage(),
                    exception
            );
        }

        // 2. Analyse Google PageSpeed / Lighthouse
        LighthouseExtractor.LighthouseAnalysis lighthouse = null;
        WebAuditResult.EcoIndexResult ecoIndex = null;

        String lighthouseStatus = "UNAVAILABLE";
        String lighthouseError = null;

        try {
            JsonNode response =
                    pageSpeedService.analyze(uri.toString());

            lighthouse = lighthouseExtractor.extract(response);
            lighthouseStatus = "AVAILABLE";

            // 3. Calcul EcoIndex si les métriques réseau existent
            LighthouseExtractor.NetworkMetrics metrics =
                    lighthouse.networkMetrics();

            if (metrics != null
                    && metrics.totalRequests() != null
                    && metrics.totalTransferBytes() != null) {

                double pageSizeKb =
                        metrics.totalTransferBytes() / 1024.0;

                EcoIndexCalculator.EcoIndexResult result =
                        ecoIndexCalculator.calculate(
                                dom.totalElements(),
                                metrics.totalRequests(),
                                pageSizeKb
                        );

                ecoIndex = new WebAuditResult.EcoIndexResult(
                        result.score(),
                        result.grade(),
                        result.co2Grams(),
                        result.waterCl()
                );
            }

        } catch (RestClientException
                 | IllegalStateException
                 | IllegalArgumentException exception) {

            lighthouseStatus = "UNAVAILABLE";
            lighthouseError = exception.getMessage();
        }

        return new WebAuditResult(
                dom,
                lighthouse,
                ecoIndex,
                lighthouseStatus,
                lighthouseError
        );
    }

    private URI validateUrl(String rawUrl) {
        try {
            if (rawUrl == null || rawUrl.isBlank()) {
                throw new IllegalArgumentException(
                        "L'URL est obligatoire."
                );
            }

            URI uri = new URI(rawUrl.trim()).normalize();

            if (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException(
                        "Seules les URL HTTP et HTTPS sont acceptées."
                );
            }

            if (uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("URL invalide.");
            }

            // Protection de base contre les accès aux réseaux privés.
            InetAddress[] addresses =
                    InetAddress.getAllByName(uri.getHost());

            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {

                    throw new IllegalArgumentException(
                            "Cette adresse ne peut pas être analysée."
                    );
                }
            }

            return uri;

        } catch (URISyntaxException | IOException exception) {
            throw new IllegalArgumentException(
                    "L'adresse URL est invalide ou inaccessible.",
                    exception
            );
        }
    }
}
