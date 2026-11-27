package com.greenops.monitoring.dto;

import jakarta.validation.constraints.DecimalMin;

public record SubscriptionUpdateRequest(
        Boolean active,
        @DecimalMin("0.1") Double thresholdDelta,
        String frequencyCron) {}
