package com.greenops.cloudsim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "carbon_intensity_cache")
public class CarbonIntensityCache {

    @Id
    @Column(name = "zone_code", length = 32, nullable = false)
    private String zoneCode;

    @Column(name = "carbon_intensity_gco2_kwh", nullable = false)
    private double carbonIntensityGco2Kwh;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public CarbonIntensityCache() {
    }

    public CarbonIntensityCache(String zoneCode, double carbonIntensityGco2Kwh, Instant fetchedAt) {
        this.zoneCode = zoneCode;
        this.carbonIntensityGco2Kwh = carbonIntensityGco2Kwh;
        this.fetchedAt = fetchedAt;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public double getCarbonIntensityGco2Kwh() {
        return carbonIntensityGco2Kwh;
    }

    public void setCarbonIntensityGco2Kwh(double carbonIntensityGco2Kwh) {
        this.carbonIntensityGco2Kwh = carbonIntensityGco2Kwh;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}