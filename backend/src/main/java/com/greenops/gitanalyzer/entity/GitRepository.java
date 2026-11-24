package com.greenops.gitanalyzer.entity;

import com.greenops.gitanalyzer.model.GitProvider;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "git_repositories")
public class GitRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GitProvider provider;

    @Column(nullable = false, length = 120)
    private String owner;

    @Column(nullable = false, length = 120)
    private String repoName;

    @Column(name = "normalized_url", nullable = false, unique = true, length = 255)
    private String normalizedUrl;

    @Column(name = "total_energy_kwh", nullable = false)
    private double totalEnergyKwh;

    @Column(name = "total_co2_grams", nullable = false)
    private double totalCo2Grams;

    @Column(name = "failure_rate_percent", nullable = false)
    private double failureRatePercent;

    @Column(name = "recommendations_joined", length = 2000)
    private String recommendationsJoined;

    @Column(name = "last_analyzed_at", nullable = false)
    private Instant lastAnalyzedAt;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CiPipelineRun> runs = new ArrayList<>();

    public GitRepository() {}

    public Long getId() { return id; }
    public GitProvider getProvider() { return provider; }
    public void setProvider(GitProvider provider) { this.provider = provider; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getNormalizedUrl() { return normalizedUrl; }
    public void setNormalizedUrl(String normalizedUrl) { this.normalizedUrl = normalizedUrl; }
    public double getTotalEnergyKwh() { return totalEnergyKwh; }
    public void setTotalEnergyKwh(double totalEnergyKwh) { this.totalEnergyKwh = totalEnergyKwh; }
    public double getTotalCo2Grams() { return totalCo2Grams; }
    public void setTotalCo2Grams(double totalCo2Grams) { this.totalCo2Grams = totalCo2Grams; }
    public double getFailureRatePercent() { return failureRatePercent; }
    public void setFailureRatePercent(double failureRatePercent) { this.failureRatePercent = failureRatePercent; }
    public String getRecommendationsJoined() { return recommendationsJoined; }
    public void setRecommendationsJoined(String recommendationsJoined) { this.recommendationsJoined = recommendationsJoined; }
    public Instant getLastAnalyzedAt() { return lastAnalyzedAt; }
    public void setLastAnalyzedAt(Instant lastAnalyzedAt) { this.lastAnalyzedAt = lastAnalyzedAt; }
    public List<CiPipelineRun> getRuns() { return runs; }
}