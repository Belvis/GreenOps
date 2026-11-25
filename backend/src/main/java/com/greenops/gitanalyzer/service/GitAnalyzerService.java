package com.greenops.gitanalyzer.service;

import com.greenops.gitanalyzer.dto.*;
import com.greenops.gitanalyzer.entity.CiPipelineRun;
import com.greenops.gitanalyzer.entity.GitRepository;
import com.greenops.gitanalyzer.parser.GitUrlParser;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import com.greenops.gitanalyzer.repository.GitRepositoryJpaRepository;
import com.greenops.gitanalyzer.strategy.GitProviderStrategy;
import com.greenops.gitanalyzer.strategy.GitStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class GitAnalyzerService {

    private final GitUrlParser urlParser;
    private final GitStrategyFactory strategyFactory;
    private final CiCarbonCalculatorService calculatorService;
    private final GitRepositoryJpaRepository repositoryJpa;

    public GitAnalyzerService(GitUrlParser urlParser,
                              GitStrategyFactory strategyFactory,
                              CiCarbonCalculatorService calculatorService,
                              GitRepositoryJpaRepository repositoryJpa) {
        this.urlParser = urlParser;
        this.strategyFactory = strategyFactory;
        this.calculatorService = calculatorService;
        this.repositoryJpa = repositoryJpa;
    }

    @Transactional
    public GitAnalysisResultDto analyzePublicRepository(String repositoryUrl) {
        ParsedGitUrl parsedUrl = urlParser.parse(repositoryUrl);
        GitProviderStrategy strategy = strategyFactory.getStrategy(parsedUrl.provider());
        List<CiRunRawData> rawRuns = strategy.fetchRecentPipelineRuns(parsedUrl);
        GitAnalysisResultDto result = calculatorService.evaluateRepository(parsedUrl, rawRuns);

        persistAnalysis(result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<GitHistoryItemDto> getAnalysisHistory() {
        return repositoryJpa.findAllByOrderByLastAnalyzedAtDesc().stream()
                .map(repo -> new GitHistoryItemDto(
                        repo.getId(),
                        repo.getProvider(),
                        repo.getOwner(),
                        repo.getRepoName(),
                        repo.getNormalizedUrl(),
                        repo.getRuns().size(),
                        repo.getTotalEnergyKwh(),
                        repo.getTotalCo2Grams(),
                        repo.getFailureRatePercent(),
                        repo.getLastAnalyzedAt(),
                        splitRecommendations(repo.getRecommendationsJoined())
                ))
                .toList();
    }

    private void persistAnalysis(GitAnalysisResultDto result) {
        GitRepository entity = repositoryJpa.findByNormalizedUrl(result.normalizedUrl())
                .orElseGet(GitRepository::new);

        entity.setProvider(result.provider());
        entity.setOwner(result.owner());
        entity.setRepoName(result.repo());
        entity.setNormalizedUrl(result.normalizedUrl());
        entity.setTotalEnergyKwh(result.totalEnergyKwh());
        entity.setTotalCo2Grams(result.totalCo2Grams());
        entity.setFailureRatePercent(result.failureRatePercent());
        entity.setRecommendationsJoined(String.join("||", result.recommendations()));
        entity.setLastAnalyzedAt(Instant.now());

        entity.getRuns().clear();
        for (CiRunEnrichedDto runDto : result.runs()) {
            CiPipelineRun runEntity = new CiPipelineRun();
            runEntity.setRepository(entity);
            runEntity.setExternalRunId(runDto.externalRunId());
            runEntity.setWorkflowName(runDto.workflowName());
            runEntity.setStatus(runDto.status());
            runEntity.setConclusion(runDto.conclusion());
            runEntity.setDurationSeconds(runDto.durationSeconds());
            runEntity.setRunnerOs(runDto.runnerOs());
            runEntity.setEnergyKwh(runDto.energyKwh());
            runEntity.setCo2Grams(runDto.co2Grams());
            runEntity.setRunCreatedAt(runDto.createdAt());
            entity.getRuns().add(runEntity);
        }

        repositoryJpa.save(entity);
    }

    private List<String> splitRecommendations(String joined) {
        if (joined == null || joined.isBlank()) {
            return List.of();
        }
        return Arrays.asList(joined.split("\\|\\|"));
    }
}