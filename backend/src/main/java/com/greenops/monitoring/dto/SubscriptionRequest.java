package com.greenops.monitoring.dto;

import com.greenops.monitoring.TargetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
        @NotNull TargetType targetType,
        @NotBlank String targetId,
        String frequencyCron,
        @DecimalMin("0.1") Double thresholdDelta) {}
