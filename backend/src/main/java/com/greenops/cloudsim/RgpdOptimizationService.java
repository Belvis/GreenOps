package com.greenops.cloudsim;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RgpdOptimizationService {

    private final CloudRegionRepository regionRepository;
    private final CarbonIntensityCacheService cacheService;
    private final CloudEnergyEngine energyEngine;

    public RgpdOptimizationService(
            CloudRegionRepository regionRepository,
            CarbonIntensityCacheService cacheService,
            CloudEnergyEngine energyEngine
    ) {
        this.regionRepository = regionRepository;
        this.cacheService = cacheService;
        this.energyEngine = energyEngine;
    }

    public SimulationComparisonResult simulate(
            int vcpu,
            double ramGb,
            double storageGb,
            double hoursPerDay,
            double durationDays,
            boolean strictRgpd
    ) {
        List<CloudRegion> regions = regionRepository.findAll();
        if (regions.isEmpty()) {
            throw new IllegalStateException("No cloud regions configured in database");
        }

        double itPowerKw = energyEngine.calculateItPowerKw(vcpu, ramGb, storageGb);

        List<RegionSimulationResult> results = regions.stream()
                .map(region -> evaluateRegion(region, itPowerKw, hoursPerDay, durationDays, strictRgpd))
                .sorted(Comparator.comparingDouble(RegionSimulationResult::carbonGrams))
                .toList();

        double averageCarbonGrams = results.stream()
                .mapToDouble(RegionSimulationResult::carbonGrams)
                .average()
                .orElse(0.0);

        RegionSimulationResult recommended = selectRecommended(results, strictRgpd);

        double deltaGrams = recommended.carbonGrams() - averageCarbonGrams;
        double deltaPercent = averageCarbonGrams > 0.0
                ? (deltaGrams / averageCarbonGrams) * 100.0
                : 0.0;

        return new SimulationComparisonResult(
                itPowerKw,
                averageCarbonGrams,
                recommended,
                deltaGrams,
                deltaPercent,
                results
        );
    }

    private RegionSimulationResult evaluateRegion(
            CloudRegion region,
            double itPowerKw,
            double hoursPerDay,
            double durationDays,
            boolean strictRgpd
    ) {
        double intensity = cacheService.getCarbonIntensityGco2PerKwh(region.getZoneCode());
        double energyKwh = energyEngine.calculateEnergyKwh(
                itPowerKw,
                region.getPue(),
                hoursPerDay,
                durationDays
        );
        double carbonGrams = energyEngine.calculateCarbonGrams(energyKwh, intensity);

        GdprStatus status = region.getGdprStatus();
        boolean eligible = !strictRgpd || (status != null && status.isCompliantInStrictMode());

        return new RegionSimulationResult(
                region.getZoneCode(),
                region.getDisplayName(),
                region.getCountry(),
                region.getPue(),
                status,
                region.getLegalNote(),
                intensity,
                itPowerKw,
                energyKwh,
                carbonGrams,
                eligible
        );
    }

    private RegionSimulationResult selectRecommended(
            List<RegionSimulationResult> sortedResults,
            boolean strictRgpd
    ) {
        if (strictRgpd) {
            return sortedResults.stream()
                    .filter(RegionSimulationResult::eligibleForRecommendation)
                    .findFirst()
                    .orElseGet(() -> sortedResults.get(0));
        }
        return sortedResults.get(0);
    }
}