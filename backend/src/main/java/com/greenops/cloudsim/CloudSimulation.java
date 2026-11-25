package com.greenops.cloudsim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cloud_simulations")
public class CloudSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "vcpu", nullable = false)
    private int vcpu;

    @Column(name = "ram_gb", nullable = false)
    private double ramGb;

    @Column(name = "storage_gb", nullable = false)
    private double storageGb;

    @Column(name = "hours_per_day", nullable = false)
    private double hoursPerDay;

    @Column(name = "duration_days", nullable = false)
    private double durationDays;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CloudSimulation() {
    }

    public CloudSimulation(
            Long id,
            Long userId,
            String name,
            int vcpu,
            double ramGb,
            double storageGb,
            double hoursPerDay,
            double durationDays,
            boolean favorite,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.vcpu = vcpu;
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.hoursPerDay = hoursPerDay;
        this.durationDays = durationDays;
        this.favorite = favorite;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVcpu() {
        return vcpu;
    }

    public void setVcpu(int vcpu) {
        this.vcpu = vcpu;
    }

    public double getRamGb() {
        return ramGb;
    }

    public void setRamGb(double ramGb) {
        this.ramGb = ramGb;
    }

    public double getStorageGb() {
        return storageGb;
    }

    public void setStorageGb(double storageGb) {
        this.storageGb = storageGb;
    }

    public double getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(double hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(double durationDays) {
        this.durationDays = durationDays;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}