package com.greenops.monitoring.dto;

import com.greenops.monitoring.MonitoringSubscription;
import com.greenops.monitoring.TargetType;
import java.time.Instant;

public record SubscriptionResponse(
        Long id, TargetType targetType, String targetId, String frequencyCron,
        double thresholdDelta, boolean active, Instant lastCheckedAt, Double lastScore) {

    public static SubscriptionResponse from(MonitoringSubscription s) {
        return new SubscriptionResponse(s.getId(), s.getTargetType(), s.getTargetId(),
                s.getFrequencyCron(), s.getThresholdDelta(), s.isActive(),
                s.getLastCheckedAt(), s.getLastScore());
    }
}
