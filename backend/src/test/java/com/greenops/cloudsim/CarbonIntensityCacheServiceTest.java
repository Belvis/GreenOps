package com.greenops.cloudsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CarbonIntensityCacheServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-25T14:00:00Z");

    private static Clock fixedClock(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private static CarbonIntensityCacheRepository inMemoryRepository(Map<String, CarbonIntensityCache> store) {
        return (CarbonIntensityCacheRepository) Proxy.newProxyInstance(
                CarbonIntensityCacheRepository.class.getClassLoader(),
                new Class<?>[]{CarbonIntensityCacheRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name) && args != null && args.length == 1) {
                        return Optional.ofNullable(store.get(args[0]));
                    }
                    if ("save".equals(name) && args != null && args.length == 1) {
                        CarbonIntensityCache entity = (CarbonIntensityCache) args[0];
                        store.put(entity.getZoneCode(), entity);
                        return entity;
                    }
                    throw new UnsupportedOperationException("Unexpected repository method: " + name);
                }
        );
    }

    private static ElectricityMapsClient stubClient(
            boolean configured,
            AtomicInteger calls,
            AtomicReference<String> lastZone,
            java.util.function.Function<String, Double> handler
    ) {
        return new ElectricityMapsClient(null, null, configured ? "test-key" : "", "http://localhost") {
            @Override
            public boolean isConfigured() {
                return configured;
            }

            @Override
            public double fetchLatestCarbonIntensity(String zoneCode) {
                calls.incrementAndGet();
                if (lastZone != null) {
                    lastZone.set(zoneCode);
                }
                return handler.apply(zoneCode);
            }
        };
    }

    @Test
    void shouldRejectInvalidFallbackIntensities() {
        Map<String, CarbonIntensityCache> store = new HashMap<>();
        CarbonIntensityCacheRepository repo = inMemoryRepository(store);
        ElectricityMapsClient client = stubClient(false, new AtomicInteger(), null, z -> 0.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new CarbonIntensityCacheService(repo, client, fixedClock(FIXED_NOW), 0.0, Map.of("FR", 55.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CarbonIntensityCacheService(repo, client, fixedClock(FIXED_NOW), -10.0, Map.of("FR", 55.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CarbonIntensityCacheService(repo, client, fixedClock(FIXED_NOW), Double.NaN, Map.of("FR", 55.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CarbonIntensityCacheService(repo, client, fixedClock(FIXED_NOW), 250.0, Map.of("FR", 0.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CarbonIntensityCacheService(repo, client, fixedClock(FIXED_NOW), 250.0, Map.of("DE", -50.0))
        );
    }

    @Test
    void shouldReturnFreshCachedValueWithoutCallingClientAndNormalizeZone() {
        Map<String, CarbonIntensityCache> store = new HashMap<>();
        CarbonIntensityCache cached = new CarbonIntensityCache();
        cached.setZoneCode("FR");
        cached.setCarbonIntensityGco2Kwh(48.5);
        cached.setFetchedAt(FIXED_NOW.minusSeconds(1800));
        store.put("FR", cached);

        AtomicInteger calls = new AtomicInteger();
        CarbonIntensityCacheService service = new CarbonIntensityCacheService(
                inMemoryRepository(store),
                stubClient(true, calls, null, z -> 999.0),
                fixedClock(FIXED_NOW)
        );

        double result = service.getCarbonIntensityGco2PerKwh("  fr ");
        assertEquals(48.5, result, 1e-9);
        assertEquals(0, calls.get());
    }

    @Test
    void shouldFetchAndCacheWhenMissingOrExpired() {
        Map<String, CarbonIntensityCache> store = new HashMap<>();
        CarbonIntensityCache expired = new CarbonIntensityCache();
        expired.setZoneCode("DE");
        expired.setCarbonIntensityGco2Kwh(320.0);
        expired.setFetchedAt(FIXED_NOW.minusSeconds(3700));
        store.put("DE", expired);

        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> lastZone = new AtomicReference<>();
        CarbonIntensityCacheService service = new CarbonIntensityCacheService(
                inMemoryRepository(store),
                stubClient(true, calls, lastZone, z -> 285.0),
                fixedClock(FIXED_NOW)
        );

        double result = service.getCarbonIntensityGco2PerKwh(" de ");
        assertEquals(285.0, result, 1e-9);
        assertEquals(1, calls.get());
        assertEquals("DE", lastZone.get());
        assertEquals(285.0, store.get("DE").getCarbonIntensityGco2Kwh(), 1e-9);
        assertEquals(FIXED_NOW, store.get("DE").getFetchedAt());
    }

    @Test
    void shouldFallbackToStaleCacheWhenClientFails() {
        Map<String, CarbonIntensityCache> store = new HashMap<>();
        CarbonIntensityCache stale = new CarbonIntensityCache();
        stale.setZoneCode("SE");
        stale.setCarbonIntensityGco2Kwh(22.0);
        stale.setFetchedAt(FIXED_NOW.minusSeconds(7200));
        store.put("SE", stale);

        AtomicInteger calls = new AtomicInteger();
        CarbonIntensityCacheService service = new CarbonIntensityCacheService(
                inMemoryRepository(store),
                stubClient(true, calls, null, z -> {
                    throw new IllegalStateException("API down");
                }),
                fixedClock(FIXED_NOW)
        );

        double result = service.getCarbonIntensityGco2PerKwh("se");
        assertEquals(22.0, result, 1e-9);
        assertEquals(1, calls.get());
    }

    @Test
    void shouldFallbackToConfiguredZoneOrGenericIntensityWhenNoCacheEntryExists() {
        Map<String, CarbonIntensityCache> store = new HashMap<>();
        AtomicInteger calls = new AtomicInteger();
        CarbonIntensityCacheService service = new CarbonIntensityCacheService(
                inMemoryRepository(store),
                stubClient(false, calls, null, z -> {
                    throw new IllegalStateException("Not configured");
                }),
                fixedClock(FIXED_NOW),
                190.0,
                Map.of("fr", 60.0)
        );

        assertEquals(60.0, service.getCarbonIntensityGco2PerKwh("FR"), 1e-9);
        assertEquals(190.0, service.getCarbonIntensityGco2PerKwh("UNKNOWN-ZONE"), 1e-9);
        assertEquals(2, calls.get());
    }
}