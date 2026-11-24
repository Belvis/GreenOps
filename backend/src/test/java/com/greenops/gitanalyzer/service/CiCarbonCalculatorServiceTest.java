package com.greenops.gitanalyzer.service;

import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.dto.GitAnalysisResultDto;
import com.greenops.gitanalyzer.model.GitProvider;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CiCarbonCalculatorServiceTest {

    private final CiCarbonCalculatorService calculator = new CiCarbonCalculatorService();

    @Test
    void shouldCalculateExactEnergyAndCo2ForLinuxRunner() {
        // 3600s = 1h * 0.15 kW * 1.2 PUE = 0.18 kWh
        double energy = calculator.calculateEnergyKwh(3600, "LINUX", 1.2);
        // 0.18 kWh * 300 gCO2/kWh = 54.0 gCO2
        double co2 = calculator.calculateCo2Grams(energy, 300.0);

        assertEquals(0.18, energy, 0.0001);
        assertEquals(54.0, co2, 0.01);
    }

    @Test
    void shouldTriggerRecommendationsForLongFailingMacOsBuilds() {
        ParsedGitUrl repo = new ParsedGitUrl(GitProvider.GITHUB, "Belvis", "GreenOps", "https://github.com/Belvis/GreenOps");
        List<CiRunRawData> runs = List.of(
                new CiRunRawData("1", "CI Build", "completed", "failure", 400L, "MACOS", Instant.now()),
                new CiRunRawData("2", "CI Build", "completed", "success", 200L, "LINUX", Instant.now())
        );

        GitAnalysisResultDto result = calculator.evaluateRepository(repo, runs);

        assertEquals(2, result.analyzedRunsCount());
        assertEquals(600L, result.totalDurationSeconds());
        assertEquals(50.0, result.failureRatePercent(), 0.01);
        assertTrue(result.totalEnergyKwh() > 0);
        assertTrue(result.totalCo2Grams() > 0);
        assertEquals(3, result.recommendations().size());
    }

    @Test
    void shouldReturnSobrietyPraiseWhenBuildsAreFastAndClean() {
        ParsedGitUrl repo = new ParsedGitUrl(GitProvider.GITHUB, "Belvis", "GreenOps", "https://github.com/Belvis/GreenOps");
        List<CiRunRawData> runs = List.of(
                new CiRunRawData("1", "Fast Test", "completed", "success", 45L, "LINUX", Instant.now())
        );

        GitAnalysisResultDto result = calculator.evaluateRepository(repo, runs);

        assertEquals(0.0, result.failureRatePercent(), 0.01);
        assertEquals(1, result.recommendations().size());
        assertTrue(result.recommendations().get(0).contains("Bonne sobriété"));
    }
}