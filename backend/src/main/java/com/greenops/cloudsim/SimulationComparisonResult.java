package com.greenops.cloudsim;

import java.util.List;

public record SimulationComparisonResult(
        double itPowerKw,
        double averageCarbonGrams,
        RegionSimulationResult recommendedRegion,
        double deltaCarbonGramsVsAverage,
        double deltaCarbonPercentVsAverage,
        List<RegionSimulationResult> rankedRegions
) {
}