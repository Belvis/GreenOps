package com.greenops.cloudsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CloudSimulatorControllerTest {

    private static CloudSimulationRepository inMemorySimulationRepository(Map<Long, CloudSimulation> store) {
        AtomicLong seq = new AtomicLong(1);
        return (CloudSimulationRepository) Proxy.newProxyInstance(
                CloudSimulationRepository.class.getClassLoader(),
                new Class<?>[]{CloudSimulationRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name) && args != null && args.length == 1) {
                        CloudSimulation entity = (CloudSimulation) args[0];
                        if (entity.getId() == null) {
                            entity.setId(seq.getAndIncrement());
                        }
                        store.put(entity.getId(), entity);
                        return entity;
                    }
                    if ("findByFavoriteTrue".equals(name)) {
                        return store.values().stream()
                                .filter(CloudSimulation::isFavorite)
                                .toList();
                    }
                    if ("findByFavoriteTrueAndUserId".equals(name) && args != null && args.length == 1) {
                        Long userId = (Long) args[0];
                        return store.values().stream()
                                .filter(s -> s.isFavorite() && userId.equals(s.getUserId()))
                                .toList();
                    }
                    if ("existsById".equals(name) && args != null && args.length == 1) {
                        return store.containsKey(args[0]);
                    }
                    if ("deleteById".equals(name) && args != null && args.length == 1) {
                        return store.remove(args[0]) != null;
                    }
                    throw new UnsupportedOperationException("Unexpected repository method: " + name);
                }
        );
    }

    private static CloudRegionRepository emptyRegionRepository() {
        return (CloudRegionRepository) Proxy.newProxyInstance(
                CloudRegionRepository.class.getClassLoader(),
                new Class<?>[]{CloudRegionRepository.class},
                (proxy, method, args) -> {
                    if ("findAll".equals(method.getName())) {
                        return new ArrayList<CloudRegion>();
                    }
                    if ("findByGdprStatus".equals(method.getName())) {
                        return new ArrayList<CloudRegion>();
                    }
                    throw new UnsupportedOperationException("Unexpected region repository method: " + method.getName());
                }
        );
    }

    @Test
    void shouldSaveAndFilterFavoritesByUserId() {
        Map<Long, CloudSimulation> store = new HashMap<>();
        CloudSimulatorController controller = new CloudSimulatorController(
                null,
                emptyRegionRepository(),
                inMemorySimulationRepository(store)
        );

        ResponseEntity<CloudSimulation> created1 = controller.saveFavorite(
                new SaveFavoriteRequest(10L, "  Prod API  ", 4, 16.0, 200.0, 24.0, 30.0)
        );
        ResponseEntity<CloudSimulation> created2 = controller.saveFavorite(
                new SaveFavoriteRequest(20L, null, 2, 8.0, 100.0, 12.0, 15.0)
        );

        assertEquals(HttpStatus.CREATED, created1.getStatusCode());
        assertEquals("Prod API", created1.getBody().getName());
        assertTrue(created1.getBody().isFavorite());

        assertEquals(HttpStatus.CREATED, created2.getStatusCode());
        assertEquals("Simulation 2vCPU / 8.0Go", created2.getBody().getName());

        List<CloudSimulation> user10Favorites = controller.getFavorites(10L).getBody();
        assertEquals(1, user10Favorites.size());
        assertEquals("Prod API", user10Favorites.get(0).getName());

        List<CloudSimulation> allFavorites = controller.getFavorites(null).getBody();
        assertEquals(2, allFavorites.size());
    }

    @Test
    void shouldDeleteFavoriteWhenPresentAndReturnNotFoundWhenMissing() {
        Map<Long, CloudSimulation> store = new HashMap<>();
        CloudSimulatorController controller = new CloudSimulatorController(
                null,
                emptyRegionRepository(),
                inMemorySimulationRepository(store)
        );

        ResponseEntity<CloudSimulation> created = controller.saveFavorite(
                new SaveFavoriteRequest(10L, "Temp Sim", 2, 4.0, 50.0, 8.0, 10.0)
        );
        Long id = created.getBody().getId();

        assertEquals(HttpStatus.NO_CONTENT, controller.deleteFavorite(id).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteFavorite(id).getStatusCode());
        assertEquals(Optional.empty(), Optional.ofNullable(store.get(id)));
    }

    @Test
    void shouldRejectInvalidFavoriteParameters() {
        Map<Long, CloudSimulation> store = new HashMap<>();
        CloudSimulatorController controller = new CloudSimulatorController(
                null,
                emptyRegionRepository(),
                inMemorySimulationRepository(store)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.saveFavorite(new SaveFavoriteRequest(1L, "Bad vCPU", 0, 8.0, 100.0, 24.0, 30.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.saveFavorite(new SaveFavoriteRequest(1L, "Bad RAM", 2, -4.0, 100.0, 24.0, 30.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.saveFavorite(new SaveFavoriteRequest(1L, "Bad Hours", 2, 8.0, 100.0, 0.0, 30.0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.saveFavorite(new SaveFavoriteRequest(1L, "NaN Days", 2, 8.0, 100.0, 24.0, Double.NaN))
        );
    }
}