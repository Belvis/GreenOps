package com.greenops.cloudsim;

public record RegionSimulationResult(
        String zoneCode,
        String displayName,
        String country,
        double pue,
        GdprStatus gdprStatus,
        String legalNote,
        double carbonIntensityGco2PerKwh,
        double itPowerKw,
        double energyKwh,
        double carbonGrams,
        boolean eligibleForRecommendation
) {
}