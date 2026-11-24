package com.greenops.gitanalyzer.dto;

import com.greenops.gitanalyzer.model.GitProvider;

import java.util.List;

public record GitAnalysisResultDto(
        GitProvider provider,
        String owner,
        String repo,
        String normalizedUrl,
        int analyzedRunsCount,
        long totalDurationSeconds,
        double totalEnergyKwh,
        double totalCo2Grams,
        double failureRatePercent,
        List<CiRunEnrichedDto> runs,
        List<String> recommendations
) {}