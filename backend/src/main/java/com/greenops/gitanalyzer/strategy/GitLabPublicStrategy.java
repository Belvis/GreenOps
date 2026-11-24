package com.greenops.gitanalyzer.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.model.GitProvider;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitLabPublicStrategy implements GitProviderStrategy {

    private static final String GITLAB_API_BASE = "https://gitlab.com/api/v4";
    private final RestClient restClient;

    public GitLabPublicStrategy(RestClient gitAnalyzerRestClient) {
        this.restClient = gitAnalyzerRestClient;
    }

    @Override
    public GitProvider getSupportedProvider() {
        return GitProvider.GITLAB;
    }

    @Override
    public List<CiRunRawData> fetchRecentPipelineRuns(ParsedGitUrl parsedUrl) {
        String encodedProject = URLEncoder.encode(parsedUrl.fullName(), StandardCharsets.UTF_8);
        URI listUri = URI.create(String.format("%s/projects/%s/pipelines?per_page=10", GITLAB_API_BASE, encodedProject));

        JsonNode pipelineList;
        try {
            pipelineList = restClient.get()
                    .uri(listUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Impossible d'interroger l'API publique GitLab pour " + parsedUrl.fullName(), e);
        }

        List<CiRunRawData> runs = new ArrayList<>();
        if (pipelineList == null || !pipelineList.isArray()) {
            return runs;
        }

        for (JsonNode summary : pipelineList) {
            String pipelineId = summary.path("id").asText();
            if (pipelineId.isBlank()) {
                continue;
            }

            JsonNode detail = fetchPipelineDetail(encodedProject, pipelineId);
            String ref = detail.path("ref").asText("main");
            String status = detail.path("status").asText("unknown");
            long durationSeconds = Math.max(0, detail.path("duration").asLong(0));
            String createdAtRaw = detail.path("created_at").asText(null);
            Instant createdAt = (createdAtRaw != null && !createdAtRaw.isBlank())
                    ? Instant.parse(createdAtRaw)
                    : Instant.now();

            runs.add(new CiRunRawData(
                    pipelineId,
                    "GitLab Pipeline (" + ref + ")",
                    status,
                    status,
                    durationSeconds,
                    "LINUX",
                    createdAt
            ));
        }

        return runs;
    }

    private JsonNode fetchPipelineDetail(String encodedProject, String pipelineId) {
        URI detailUri = URI.create(String.format("%s/projects/%s/pipelines/%s", GITLAB_API_BASE, encodedProject, pipelineId));
        try {
            return restClient.get()
                    .uri(detailUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Erreur lors de la récupération du pipeline GitLab #" + pipelineId, e);
        }
    }
}