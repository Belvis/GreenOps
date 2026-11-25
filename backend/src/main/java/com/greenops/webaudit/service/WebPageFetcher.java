
package com.greenops.webaudit.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class WebPageFetcher {

    public FetchedPage fetch(URI uri) throws IOException {

        Connection.Response response = Jsoup.connect(uri.toString())
                .userAgent("Mozilla/5.0 (compatible; GreenOps-WebAudit/1.0)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8")
                .timeout(30_000)
                .maxBodySize(2_000_000)
                .followRedirects(true)
                .execute();

        byte[] bodyBytes = response.bodyAsBytes();
        Document document = response.parse();

        return new FetchedPage(
                response.statusCode(),
                document,
                bodyBytes.length
        );
    }

    public record FetchedPage(
            int statusCode,
            Document document,
            long bodySizeBytes
    ) {}
}
