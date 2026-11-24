
package com.greenops.webaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LighthouseExtractor {

    public LighthouseAnalysis extract(JsonNode response) {

        if (response == null
                || response.path("lighthouseResult").isMissingNode()) {
            throw new IllegalArgumentException(
                    "La réponse Google ne contient pas de résultat Lighthouse."
            );
        }

        JsonNode lighthouse = response.path("lighthouseResult");
        JsonNode categories = lighthouse.path("categories");
        JsonNode audits = lighthouse.path("audits");

        List<CategoryScore> scores = new ArrayList<>();

        addCategory(scores, categories, "performance");
        addCategory(scores, categories, "accessibility");
        addCategory(scores, categories, "best-practices");
        addCategory(scores, categories, "seo");

        List<Recommendation> recommendations = new ArrayList<>();

        audits.fields().forEachRemaining(entry -> {
            String id = entry.getKey();
            JsonNode audit = entry.getValue();

            JsonNode scoreNode = audit.get("score");

            if (scoreNode == null || !scoreNode.isNumber()
                    || scoreNode.asDouble() >= 1) {
                return;
            }

            String scoreDisplayMode =
                    audit.path("scoreDisplayMode").asText("");

            if ("notApplicable".equals(scoreDisplayMode)
                    || "manual".equals(scoreDisplayMode)
                    || "informative".equals(scoreDisplayMode)) {
                return;
            }

            String title = audit.path("title").asText("");
            String description = audit.path("description").asText("");

            if (title.isBlank()) {
                return;
            }

            JsonNode details = audit.path("details");

            Double savingsMs = getOptionalDouble(
                    details, "overallSavingsMs"
            );

            Double savingsBytes = getOptionalDouble(
                    details, "overallSavingsBytes"
            );

            recommendations.add(new Recommendation(
                    id,
                    title,
                    description,
                    scoreNode.asDouble(),
                    audit.path("displayValue").asText(""),
                    savingsMs,
                    savingsBytes
            ));
        });

        NetworkMetrics networkMetrics = extractNetworkMetrics(audits);

        return new LighthouseAnalysis(
                lighthouse.path("finalDisplayedUrl").asText(""),
                scores,
                recommendations,
                networkMetrics
        );
    }

    private NetworkMetrics extractNetworkMetrics(JsonNode audits) {

        JsonNode networkAudit = audits.path("network-requests");
        JsonNode items = networkAudit.path("details").path("items");

        if (!items.isArray()) {
            return new NetworkMetrics(null, null);
        }

        int totalRequests = 0;
        long totalTransferBytes = 0;

        for (JsonNode item : items) {
            totalRequests++;

            JsonNode transferSize = item.get("transferSize");

            if (transferSize != null && transferSize.isNumber()) {
                totalTransferBytes += transferSize.asLong();
            }
        }

        return new NetworkMetrics(
                totalRequests,
                totalTransferBytes
        );
    }

    private void addCategory(
            List<CategoryScore> scores,
            JsonNode categories,
            String categoryId
    ) {
        JsonNode category = categories.path(categoryId);
        JsonNode score = category.get("score");

        if (score != null && score.isNumber()) {
            scores.add(new CategoryScore(
                    categoryId,
                    category.path("title").asText(""),
                    (int) Math.round(score.asDouble() * 100)
            ));
        }
    }

    private Double getOptionalDouble(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || !value.isNumber()) {
            return null;
        }

        return value.asDouble();
    }

    public record LighthouseAnalysis(
            String analyzedUrl,
            List<CategoryScore> scores,
            List<Recommendation> recommendations,
            NetworkMetrics networkMetrics
    ) {}

    public record NetworkMetrics(
            Integer totalRequests,
            Long totalTransferBytes
    ) {}

    public record CategoryScore(
            String id,
            String title,
            int score
    ) {}

    public record Recommendation(
            String id,
            String title,
            String description,
            double score,
            String displayValue,
            Double savingsMs,
            Double savingsBytes
    ) {}
}
