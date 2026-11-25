package com.greenops.cloudsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RgpdOptimizationServiceTest {

    @Test
    void shouldSelectCompliantRegionEvenWhenNonEuRegionHasLowerCarbon() {
        CloudRegionRepository regionRepo = mock(CloudRegionRepository.class);
        CarbonIntensityCacheRepository cacheRepo = mock(CarbonIntensityCacheRepository.class);
        ElectricityMapsClient client = mock(ElectricityMapsClient.class);

        Instant fixedNow = Instant.parse("2026-11-24T12:00:00Z");
        Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);
        CarbonIntensityCacheService cacheService = new CarbonIntensityCacheService(cacheRepo, client, clock);
        RgpdOptimizationService service = new RgpdOptimizationService(
                regionRepo,
                cacheService,
                new CloudEnergyEngine()
        );

        CloudRegion usEast = new CloudRegion(
                "US-EAST-1",
                "US East (N. Virginia)",
                "United States",
                1.20,
                GdprStatus.HORS_UE_CLOUD_ACT,
                "Soumis au CLOUD Act"
        );
        CloudRegion fr = new CloudRegion(
                "FR",
                "France Central",
                "France",
                1.25,
                GdprStatus.UE_RGPD,
                "Conforme RGPD"
        );
        CloudRegion de = new CloudRegion(
                "DE",
                "Germany Central",
                "Germany",
                1.22,
                GdprStatus.UE_RGPD,
                "Conforme RGPD"
        );

        when(regionRepo.findAll()).thenReturn(List.of(usEast, fr, de));

        // US-EAST-1 is lower carbon (20 g/kWh) than FR (55 g/kWh) and DE (320 g/kWh)
        when(cacheRepo.findById("US-EAST-1"))
                .thenReturn(Optional.of(new CarbonIntensityCache("US-EAST-1", 20.0, fixedNow)));
        when(cacheRepo.findById("FR"))
                .thenReturn(Optional.of(new CarbonIntensityCache("FR", 55.0, fixedNow)));
        when(cacheRepo.findById("DE"))
                .thenReturn(Optional.of(new CarbonIntensityCache("DE", 320.0, fixedNow)));

        SimulationComparisonResult result = service.simulate(4, 16.0, 200.0, 24.0, 10.0, true);

        assertEquals("FR", result.recommendedRegion().zoneCode());
        assertTrue(result.recommendedRegion().eligibleForRecommendation());
        assertFalse(result.rankedRegions().stream()
                .filter(r -> r.zoneCode().equals("US-EAST-1"))
                .findFirst()
                .orElseThrow()
                .eligibleForRecommendation());
        assertTrue(result.deltaCarbonGramsVsAverage() < 0.0);
        verify(client, never()).fetchLatestCarbonIntensity(any());
    }

    @Test
    void shouldFallbackToCachedValueWhenElectricityMapsFails() {
        CarbonIntensityCacheRepository cacheRepo = mock(CarbonIntensityCacheRepository.class);
        ElectricityMapsClient client = mock(ElectricityMapsClient.class);

        Instant now = Instant.parse("2026-11-24T12:00:00Z");
        Instant staleTimestamp = now.minusSeconds(3600 * 3); // 3 hours old (> 60 min TTL)
        CarbonIntensityCache staleEntry = new CarbonIntensityCache("FR", 62.5, staleTimestamp);

        when(cacheRepo.findById("FR")).thenReturn(Optional.of(staleEntry));
        when(client.fetchLatestCarbonIntensity("FR")).thenThrow(new IllegalStateException("API timeout"));

        CarbonIntensityCacheService cacheService = new CarbonIntensityCacheService(
                cacheRepo,
                client,
                Clock.fixed(now, ZoneOffset.UTC)
        );

        double intensity = cacheService.getCarbonIntensityGco2PerKwh("FR");
        assertEquals(62.5, intensity, 1e-9);
    }
}