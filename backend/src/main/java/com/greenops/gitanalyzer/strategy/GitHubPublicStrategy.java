package com.greenops.gitanalyzer.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.model.GitProvider;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubPublicStrategy implements GitProviderStrategy {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private final RestClient restClient;

    public GitHubPublicStrategy(RestClient gitAnalyzerRestClient) {
        this.restClient = gitAnalyzerRestClient;
    }

    @Override
    public GitProvider getSupportedProvider() {
        return GitProvider.GITHUB;
    }

    @Override
    public List<CiRunRawData> fetchRecentPipelineRuns(ParsedGitUrl parsedUrl) {
        String url = String.format("%s/repos/%s/%s/actions/runs?per_page=10",
                GITHUB_API_BASE, parsedUrl.owner(), parsedUrl.repo());

        JsonNode root;
        try {
            root = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Impossible d'interroger l'API publique GitHub pour " + parsedUrl.fullName(), e);
        }

        List<CiRunRawData> runs = new ArrayList<>();
        if (root == null || !root.has("workflow_runs") || !root.get("workflow_runs").isArray()) {
            return runs;
        }

        for (JsonNode node : root.get("workflow_runs")) {
            String runId = node.path("id").asText("unknown");
            String name = node.path("name").asText("CI Workflow");
            String status = node.path("status").asText("completed");
            String conclusion = node.path("conclusion").asText("unknown");

            Instant createdAt = parseInstantOrNow(node.path("created_at").asText(null));
            Instant startedAt = parseInstantOrDefault(node.path("run_started_at").asText(null), createdAt);
            Instant updatedAt = parseInstantOrDefault(node.path("updated_at").asText(null), startedAt);

            long durationSeconds = Math.max(0, Duration.between(startedAt, updatedAt).getSeconds());
            String runnerOs = detectRunnerOs(name);

            runs.add(new CiRunRawData(runId, name, status, conclusion, durationSeconds, runnerOs, createdAt));
        }

        return runs;
    }

    private Instant parseInstantOrNow(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        return Instant.parse(value);
    }

    private Instant parseInstantOrDefault(String value, Instant fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Instant.parse(value);
    }

    private String detectRunnerOs(String workflowName) {
        String lower = workflowName.toLowerCase();
        if (lower.contains("macos") || lower.contains("ios")) {
            return "MACOS";
        }
        if (lower.contains("windows") || lower.contains("win")) {
            return "WINDOWS";
        }
        return "LINUX";
    }
}