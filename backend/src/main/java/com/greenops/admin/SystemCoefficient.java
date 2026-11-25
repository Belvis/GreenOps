package com.greenops.admin;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "system_coefficients")
public class SystemCoefficient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_key", nullable = false, unique = true, length = 80)
    private String paramKey;

    @Column(name = "param_value", nullable = false)
    private double paramValue;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(length = 255)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public SystemCoefficient() {}

    public SystemCoefficient(String paramKey, double paramValue, String unit, String description) {
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.unit = unit;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getParamKey() { return paramKey; }
    public double getParamValue() { return paramValue; }
    public void setParamValue(double paramValue) {
        this.paramValue = paramValue;
        this.updatedAt = Instant.now();
    }
    public String getUnit() { return unit; }
    public String getDescription() { return description; }
    public Instant getUpdatedAt() { return updatedAt; }
}