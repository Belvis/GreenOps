package com.greenops.monitoring;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "monitoring_subscriptions")
public class MonitoringSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "frequency_cron", nullable = false)
    private String frequencyCron = "0 0 8 * * *";

    @Column(name = "threshold_delta", nullable = false)
    private double thresholdDelta = 10.0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    /** Dernier score connu (sert de Vt-1 pour le calcul du delta). */
    @Column(name = "last_score")
    private Double lastScore;

    public MonitoringSubscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getFrequencyCron() { return frequencyCron; }
    public void setFrequencyCron(String frequencyCron) { this.frequencyCron = frequencyCron; }
    public double getThresholdDelta() { return thresholdDelta; }
    public void setThresholdDelta(double thresholdDelta) { this.thresholdDelta = thresholdDelta; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Double getLastScore() { return lastScore; }
    public void setLastScore(Double lastScore) { this.lastScore = lastScore; }
}
