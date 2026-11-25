package com.greenops.gitanalyzer.dto;

import java.time.Instant;

public record CiRunEnrichedDto(
        String externalRunId,
        String workflowName,
        String status,
        String conclusion,
        long durationSeconds,
        String runnerOs,
        double energyKwh,
        double co2Grams,
        Instant createdAt
) {}