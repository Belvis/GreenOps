package com.greenops.gitanalyzer.dto;

import com.greenops.gitanalyzer.model.GitProvider;

import java.time.Instant;
import java.util.List;

public record GitHistoryItemDto(
        Long id,
        GitProvider provider,
        String owner,
        String repo,
        String normalizedUrl,
        int runsCount,
        double totalEnergyKwh,
        double totalCo2Grams,
        double failureRatePercent,
        Instant lastAnalyzedAt,
        List<String> recommendations
) {}