package com.greenops.gitanalyzer.service;

import com.greenops.gitanalyzer.dto.CiRunEnrichedDto;
import com.greenops.gitanalyzer.dto.CiRunRawData;
import com.greenops.gitanalyzer.dto.GitAnalysisResultDto;
import com.greenops.gitanalyzer.parser.ParsedGitUrl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CiCarbonCalculatorService {

    public static final double DEFAULT_PUE = 1.20;
    public static final double DEFAULT_CARBON_INTENSITY_G_PER_KWH = 300.0;

    public static final double POWER_LINUX_KW = 0.15;
    public static final double POWER_WINDOWS_KW = 0.22;
    public static final double POWER_MACOS_KW = 0.30;

    public double resolveRunnerPowerKw(String runnerOs) {
        if (runnerOs == null) {
            return POWER_LINUX_KW;
        }
        return switch (runnerOs.toUpperCase()) {
            case "MACOS" -> POWER_MACOS_KW;
            case "WINDOWS" -> POWER_WINDOWS_KW;
            default -> POWER_LINUX_KW;
        };
    }

    public double calculateEnergyKwh(long durationSeconds, String runnerOs, double pue) {
        if (durationSeconds <= 0) {
            return 0.0;
        }
        double hours = durationSeconds / 3600.0;
        double powerKw = resolveRunnerPowerKw(runnerOs);
        return hours * powerKw * pue;
    }

    public double calculateCo2Grams(double energyKwh, double carbonIntensity) {
        return energyKwh * carbonIntensity;
    }

    public GitAnalysisResultDto evaluateRepository(ParsedGitUrl parsedUrl, List<CiRunRawData> rawRuns) {
        List<CiRunEnrichedDto> enrichedRuns = new ArrayList<>();
        long totalDuration = 0;
        double totalEnergy = 0.0;
        double totalCo2 = 0.0;
        long failedCount = 0;
        boolean hasHeavyOsRunner = false;

        for (CiRunRawData raw : rawRuns) {
            double energy = calculateEnergyKwh(raw.durationSeconds(), raw.runnerOs(), DEFAULT_PUE);
            double co2 = calculateCo2Grams(energy, DEFAULT_CARBON_INTENSITY_G_PER_KWH);

            totalDuration += raw.durationSeconds();
            totalEnergy += energy;
            totalCo2 += co2;

            if (isFailed(raw)) {
                failedCount++;
            }
            if ("MACOS".equalsIgnoreCase(raw.runnerOs()) || "WINDOWS".equalsIgnoreCase(raw.runnerOs())) {
                hasHeavyOsRunner = true;
            }

            enrichedRuns.add(new CiRunEnrichedDto(
                    raw.externalRunId(),
                    raw.workflowName(),
                    raw.status(),
                    raw.conclusion(),
                    raw.durationSeconds(),
                    raw.runnerOs(),
                    round4(energy),
                    round2(co2),
                    raw.createdAt()
            ));
        }

        int count = rawRuns.size();
        double failureRate = count == 0 ? 0.0 : (failedCount * 100.0) / count;
        double avgDuration = count == 0 ? 0.0 : (double) totalDuration / count;

        List<String> recommendations = generateRecommendations(count, avgDuration, failureRate, hasHeavyOsRunner);

        return new GitAnalysisResultDto(
                parsedUrl.provider(),
                parsedUrl.owner(),
                parsedUrl.repo(),
                parsedUrl.normalizedUrl(),
                count,
                totalDuration,
                round4(totalEnergy),
                round2(totalCo2),
                round2(failureRate),
                enrichedRuns,
                recommendations
        );
    }

    private boolean isFailed(CiRunRawData raw) {
        String conclusion = raw.conclusion() == null ? "" : raw.conclusion().toLowerCase();
        String status = raw.status() == null ? "" : raw.status().toLowerCase();
        return conclusion.contains("fail") || status.contains("fail") || conclusion.contains("timed_out");
    }

    private List<String> generateRecommendations(int count, double avgDurationSeconds, double failureRate, boolean hasHeavyOsRunner) {
        List<String> tips = new ArrayList<>();
        if (count == 0) {
            tips.add("Aucun pipeline CI/CD récent détecté sur ce dépôt public. Vérifiez l'activation de GitHub Actions ou GitLab CI.");
            return tips;
        }
        if (avgDurationSeconds > 180) {
            tips.add("Durée moyenne élevée (" + Math.round(avgDurationSeconds) + "s) : activez la mise en cache des dépendances (actions/cache pour Maven/npm) afin de réduire le temps CPU.");
        }
        if (failureRate > 20.0) {
            tips.add("Taux d'échec CI élevé (" + round2(failureRate) + "%) : configurez des hooks de pré-commit locaux (linters/tests rapides) pour éviter des builds énergivores inutiles.");
        }
        if (hasHeavyOsRunner) {
            tips.add("Runners macOS ou Windows détectés : migrez les jobs compatibles vers ubuntu-latest (Linux), jusqu'à 2x moins énergivore.");
        }
        if (tips.isEmpty()) {
            tips.add("Bonne sobriété CI/CD : durées maîtrisées et faible taux d'échec. Pensez à déclencher les suites lourdes uniquement sur la branche principale.");
        }
        return tips;
    }

    private double round4(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}