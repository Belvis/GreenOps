package com.greenops.gitanalyzer.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ci_pipeline_runs")
public class CiPipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitRepository repository;

    @Column(name = "external_run_id", nullable = false, length = 100)
    private String externalRunId;

    @Column(name = "workflow_name", nullable = false, length = 200)
    private String workflowName;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String conclusion;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "runner_os", nullable = false, length = 30)
    private String runnerOs;

    @Column(name = "energy_kwh", nullable = false)
    private double energyKwh;

    @Column(name = "co2_grams", nullable = false)
    private double co2Grams;

    @Column(name = "run_created_at", nullable = false)
    private Instant runCreatedAt;

    public CiPipelineRun() {}

    public Long getId() { return id; }
    public GitRepository getRepository() { return repository; }
    public void setRepository(GitRepository repository) { this.repository = repository; }
    public String getExternalRunId() { return externalRunId; }
    public void setExternalRunId(String externalRunId) { this.externalRunId = externalRunId; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getRunnerOs() { return runnerOs; }
    public void setRunnerOs(String runnerOs) { this.runnerOs = runnerOs; }
    public double getEnergyKwh() { return energyKwh; }
    public void setEnergyKwh(double energyKwh) { this.energyKwh = energyKwh; }
    public double getCo2Grams() { return co2Grams; }
    public void setCo2Grams(double co2Grams) { this.co2Grams = co2Grams; }
    public Instant getRunCreatedAt() { return runCreatedAt; }
    public void setRunCreatedAt(Instant runCreatedAt) { this.runCreatedAt = runCreatedAt; }
}