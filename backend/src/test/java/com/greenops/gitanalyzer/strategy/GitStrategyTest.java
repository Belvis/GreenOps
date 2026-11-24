package com.greenops.gitanalyzer.strategy;

import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.model.GitProvider;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitStrategyTest {

    private MockRestServiceServer mockServer;
    private GitHubPublicStrategy gitHubStrategy;
    private GitLabPublicStrategy gitLabStrategy;
    private GitStrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        this.gitHubStrategy = new GitHubPublicStrategy(restClient);
        this.gitLabStrategy = new GitLabPublicStrategy(restClient);
        this.strategyFactory = new GitStrategyFactory(List.of(gitHubStrategy, gitLabStrategy));
    }

    @Test
    void shouldResolveCorrectStrategyFromFactory() {
        assertSame(gitHubStrategy, strategyFactory.getStrategy(GitProvider.GITHUB));
        assertSame(gitLabStrategy, strategyFactory.getStrategy(GitProvider.GITLAB));
    }

    @Test
    void shouldFetchAndParseGitHubWorkflowRuns() {
        String githubPayload = """
                {
                  "workflow_runs": [
                    {
                      "id": 1001,
                      "name": "Build Linux CI",
                      "status": "completed",
                      "conclusion": "success",
                      "created_at": "2026-11-24T10:00:00Z",
                      "run_started_at": "2026-11-24T10:00:00Z",
                      "updated_at": "2026-11-24T10:03:00Z"
                    },
                    {
                      "id": 1002,
                      "name": "Build macOS App",
                      "status": "completed",
                      "conclusion": "failure",
                      "created_at": "2026-11-24T11:00:00Z",
                      "run_started_at": "2026-11-24T11:00:00Z",
                      "updated_at": "2026-11-24T11:05:00Z"
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("https://api.github.com/repos/Belvis/GreenOps/actions/runs?per_page=10"))
                .andRespond(withSuccess(githubPayload, MediaType.APPLICATION_JSON));

        ParsedGitUrl parsed = new ParsedGitUrl(GitProvider.GITHUB, "Belvis", "GreenOps", "https://github.com/Belvis/GreenOps");
        List<CiRunRawData> runs = gitHubStrategy.fetchRecentPipelineRuns(parsed);

        assertEquals(2, runs.size());
        assertEquals(180L, runs.get(0).durationSeconds());
        assertEquals("LINUX", runs.get(0).runnerOs());
        assertEquals(300L, runs.get(1).durationSeconds());
        assertEquals("MACOS", runs.get(1).runnerOs());
        mockServer.verify();
    }

    @Test
    void shouldFetchAndParseGitLabPipelines() {
        String listPayload = """
                [
                  { "id": 55, "ref": "main", "status": "success" }
                ]
                """;
        String detailPayload = """
                {
                  "id": 55,
                  "ref": "main",
                  "status": "success",
                  "duration": 240,
                  "created_at": "2026-11-24T12:00:00Z"
                }
                """;

        mockServer.expect(requestTo("https://gitlab.com/api/v4/projects/Belvis%2FGreenOps/pipelines?per_page=10"))
                .andRespond(withSuccess(listPayload, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://gitlab.com/api/v4/projects/Belvis%2FGreenOps/pipelines/55"))
                .andRespond(withSuccess(detailPayload, MediaType.APPLICATION_JSON));

        ParsedGitUrl parsed = new ParsedGitUrl(GitProvider.GITLAB, "Belvis", "GreenOps", "https://gitlab.com/Belvis/GreenOps");
        List<CiRunRawData> runs = gitLabStrategy.fetchRecentPipelineRuns(parsed);

        assertEquals(1, runs.size());
        assertEquals(240L, runs.get(0).durationSeconds());
        assertEquals("LINUX", runs.get(0).runnerOs());
        mockServer.verify();
    }
}