package com.greenops.gitanalyzer.dto;

import java.time.Instant;

public record CiRunRawData(
        String externalRunId,
        String workflowName,
        String status,
        String conclusion,
        long durationSeconds,
        String runnerOs,
        Instant createdAt
) {}