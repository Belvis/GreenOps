package com.greenops.cloudsim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElectricityMapsClient {

    private static final String DEFAULT_BASE_URL = "https://api.electricitymap.org/v3/carbon-intensity/latest";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    @Autowired
    public ElectricityMapsClient(
            @Value("${greenops.apis.electricity-maps-key:}") String apiKey
    ) {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                new ObjectMapper(),
                apiKey,
                DEFAULT_BASE_URL
        );
    }

    ElectricityMapsClient(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiKey,
            String baseUrl
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    public double fetchLatestCarbonIntensity(String zoneCode) {
        if (zoneCode == null || zoneCode.isBlank()) {
            throw new IllegalArgumentException("zoneCode must not be blank");
        }
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Electricity Maps API key is not configured");
        }

        String encodedZone = URLEncoder.encode(zoneCode.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?zone=" + encodedZone);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("auth-token", apiKey)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Electricity Maps API returned HTTP" + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode intensityNode = root.path("carbonIntensity");
            if (!intensityNode.isNumber()) {
                throw new IllegalStateException("Missing numeric carbonIntensity field in Electricity Maps response");
            }

            double intensity = intensityNode.asDouble();
            if (Double.isNaN(intensity) || Double.isInfinite(intensity) || intensity < 0) {
                throw new IllegalStateException("Invalid carbonIntensity value: " + intensity);
            }
            return intensity;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to fetch carbon intensity for zone " + zoneCode, ex);
        }
    }
}