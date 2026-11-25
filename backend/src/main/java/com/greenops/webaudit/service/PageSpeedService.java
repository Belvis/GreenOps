
package com.greenops.webaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class PageSpeedService {

    private final RestClient restClient;
    private final String apiKey;

    public PageSpeedService(
            RestClient.Builder restClientBuilder,
            @Value("${greenops.apis.pagespeed-key:}") String apiKey
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        // Délai maximal pour établir la connexion : 10 secondes.
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));

        // Délai maximal pour recevoir la réponse Google : 60 secondes.
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();

        this.apiKey = apiKey;
    }

    public JsonNode analyze(String url) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La clé API Google PageSpeed est manquante."
            );
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/pagespeedonline/v5/runPagespeed")
                        .queryParam("url", url)
                        .queryParam("key", apiKey)
                        .queryParam("strategy", "mobile")
                        .queryParam("category", "PERFORMANCE")
                        .queryParam("category", "ACCESSIBILITY")
                        .queryParam("category", "BEST_PRACTICES")
                        .queryParam("category", "SEO")
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }
}
