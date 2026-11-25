package com.greenops.cloudsim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cloud_regions")
public class CloudRegion {

    @Id
    @Column(name = "zone_code", length = 32, nullable = false)
    private String zoneCode;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "pue", nullable = false)
    private double pue;

    @Enumerated(EnumType.STRING)
    @Column(name = "gdpr_status", length = 32, nullable = false)
    private GdprStatus gdprStatus;

    @Column(name = "legal_note", length = 500)
    private String legalNote;

    public CloudRegion() {
    }

    public CloudRegion(
            String zoneCode,
            String displayName,
            String country,
            double pue,
            GdprStatus gdprStatus,
            String legalNote
    ) {
        this.zoneCode = zoneCode;
        this.displayName = displayName;
        this.country = country;
        this.pue = pue;
        this.gdprStatus = gdprStatus;
        this.legalNote = legalNote;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getPue() {
        return pue;
    }

    public void setPue(double pue) {
        this.pue = pue;
    }

    public GdprStatus getGdprStatus() {
        return gdprStatus;
    }

    public void setGdprStatus(GdprStatus gdprStatus) {
        this.gdprStatus = gdprStatus;
    }

    public String getLegalNote() {
        return legalNote;
    }

    public void setLegalNote(String legalNote) {
        this.legalNote = legalNote;
    }
}