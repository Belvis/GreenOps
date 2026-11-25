package com.greenops.cloudsim;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CarbonIntensityCacheService {

    public static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private static final Logger log = LoggerFactory.getLogger(CarbonIntensityCacheService.class);

    private static final Map<String, Double> DEFAULT_FALLBACK_INTENSITY = Map.of(
            "FR", 55.0,
            "DE", 350.0,
            "SE", 25.0,
            "CH", 30.0,
            "US-EAST-1", 380.0
    );

    private static final double DEFAULT_GENERIC_FALLBACK_INTENSITY = 250.0;

    private final CarbonIntensityCacheRepository cacheRepository;
    private final ElectricityMapsClient electricityMapsClient;
    private final Clock clock;
    private final double genericFallbackIntensity;
    private final Map<String, Double> zoneFallbackIntensity;

    @Autowired
    public CarbonIntensityCacheService(
            CarbonIntensityCacheRepository cacheRepository,
            ElectricityMapsClient electricityMapsClient,
            @Value("${greenops.cloudsim.fallback.generic-intensity:250.0}") double genericFallbackIntensity,
            @Value("#{${greenops.cloudsim.fallback.zones:{'FR':55.0,'DE':350.0,'SE':25.0,'CH':30.0,'US-EAST-1':380.0}}}") Map<String, Double> zoneFallbackIntensity
    ) {
        this(
                cacheRepository,
                electricityMapsClient,
                Clock.systemUTC(),
                genericFallbackIntensity,
                zoneFallbackIntensity
        );
    }

    CarbonIntensityCacheService(
            CarbonIntensityCacheRepository cacheRepository,
            ElectricityMapsClient electricityMapsClient,
            Clock clock
    ) {
        this(
                cacheRepository,
                electricityMapsClient,
                clock,
                DEFAULT_GENERIC_FALLBACK_INTENSITY,
                DEFAULT_FALLBACK_INTENSITY
        );
    }

    CarbonIntensityCacheService(
            CarbonIntensityCacheRepository cacheRepository,
            ElectricityMapsClient electricityMapsClient,
            Clock clock,
            double genericFallbackIntensity,
            Map<String, Double> zoneFallbackIntensity
    ) {
        validatePositiveIntensity(genericFallbackIntensity, "genericFallbackIntensity");
        this.cacheRepository = cacheRepository;
        this.electricityMapsClient = electricityMapsClient;
        this.clock = clock;
        this.genericFallbackIntensity = genericFallbackIntensity;

        Map<String, Double> normalizedMap = new LinkedHashMap<>();
        if (zoneFallbackIntensity != null) {
            zoneFallbackIntensity.forEach((k, v) -> {
                if (k != null && !k.isBlank()) {
                    String zoneKey = k.trim().toUpperCase(Locale.ROOT);
                    validatePositiveIntensity(v, "zoneFallbackIntensity[" + zoneKey + "]");
                    normalizedMap.put(zoneKey, v);
                }
            });
        }
        this.zoneFallbackIntensity = Map.copyOf(normalizedMap);
    }

    public double getCarbonIntensityGco2PerKwh(String zoneCode) {
        if (zoneCode == null || zoneCode.isBlank()) {
            throw new IllegalArgumentException("zoneCode must not be blank");
        }

        String normalizedZone = zoneCode.trim().toUpperCase(Locale.ROOT);
        Instant now = clock.instant();
        Optional<CarbonIntensityCache> existingOpt = cacheRepository.findById(normalizedZone);

        if (existingOpt.isPresent() && isFresh(existingOpt.get(), now)) {
            return existingOpt.get().getCarbonIntensityGco2Kwh();
        }

        try {
            double freshIntensity = electricityMapsClient.fetchLatestCarbonIntensity(normalizedZone);
            CarbonIntensityCache updatedEntry = existingOpt.orElseGet(CarbonIntensityCache::new);
            updatedEntry.setZoneCode(normalizedZone);
            updatedEntry.setCarbonIntensityGco2Kwh(freshIntensity);
            updatedEntry.setFetchedAt(now);
            cacheRepository.save(updatedEntry);
            return freshIntensity;
        } catch (RuntimeException ex) {
            if (existingOpt.isPresent()) {
                log.warn(
                        "Electricity Maps unavailable for zone {}, falling back to cached value from {}",
                        normalizedZone,
                        existingOpt.get().getFetchedAt(),
                        ex
                );
                return existingOpt.get().getCarbonIntensityGco2Kwh();
            }
            double fallback = zoneFallbackIntensity.getOrDefault(
                    normalizedZone,
                    genericFallbackIntensity
            );
            log.warn(
                    "Electricity Maps unavailable and no cache entry for zone {}, using fallback {} gCO2/kWh",
                    normalizedZone,
                    fallback
            );
            return fallback;
        }
    }

    private boolean isFresh(CarbonIntensityCache entry, Instant now) {
        if (entry.getFetchedAt() == null) {
            return false;
        }
        Duration age = Duration.between(entry.getFetchedAt(), now);
        return !age.isNegative() && age.compareTo(CACHE_TTL) < 0;
    }

    private static void validatePositiveIntensity(Double intensity, String label) {
        if (intensity == null || Double.isNaN(intensity) || Double.isInfinite(intensity) || intensity <= 0.0) {
            throw new IllegalArgumentException(label + " must be a positive finite number, got: " + intensity);
        }
    }
}