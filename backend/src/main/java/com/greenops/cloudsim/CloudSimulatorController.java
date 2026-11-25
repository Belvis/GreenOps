package com.greenops.cloudsim;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cloud")
public class CloudSimulatorController {

    private final RgpdOptimizationService optimizationService;
    private final CloudRegionRepository regionRepository;
    private final CloudSimulationRepository simulationRepository;

    public CloudSimulatorController(
            RgpdOptimizationService optimizationService,
            CloudRegionRepository regionRepository,
            CloudSimulationRepository simulationRepository
    ) {
        this.optimizationService = optimizationService;
        this.regionRepository = regionRepository;
        this.simulationRepository = simulationRepository;
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulationComparisonResult> simulate(@RequestBody SimulateRequest request) {
        SimulationComparisonResult result = optimizationService.simulate(
                request.vcpu(),
                request.ramGb(),
                request.storageGb(),
                request.hoursPerDay(),
                request.durationDays(),
                request.effectiveStrictRgpd()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/regions")
    public ResponseEntity<List<CloudRegion>> getRegions(
            @RequestParam(name = "gdprStatus", required = false) GdprStatus gdprStatus
    ) {
        List<CloudRegion> regions = gdprStatus != null
                ? regionRepository.findByGdprStatus(gdprStatus)
                : regionRepository.findAll();
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<CloudSimulation>> getFavorites(
            @RequestParam(name = "userId", required = false) Long userId
    ) {
        List<CloudSimulation> favorites = userId != null
                ? simulationRepository.findByFavoriteTrueAndUserId(userId)
                : simulationRepository.findByFavoriteTrue();
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/favorites")
    public ResponseEntity<CloudSimulation> saveFavorite(@RequestBody SaveFavoriteRequest request) {
        validateFavoriteRequest(request);

        String resolvedName = (request.name() == null || request.name().isBlank())
                ? "Simulation " + request.vcpu() + "vCPU / " + request.ramGb() + "Go"
                : request.name().trim();

        CloudSimulation entity = new CloudSimulation(
                null,
                request.userId(),
                resolvedName,
                request.vcpu(),
                request.ramGb(),
                request.storageGb(),
                request.hoursPerDay(),
                request.durationDays(),
                true,
                Instant.now()
        );

        CloudSimulation saved = simulationRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable("id") Long id) {
        if (!simulationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        simulationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private static void validateFavoriteRequest(SaveFavoriteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.vcpu() <= 0) {
            throw new IllegalArgumentException("vcpu must be positive");
        }
        validateNonNegativeFinite(request.ramGb(), "ramGb");
        validateNonNegativeFinite(request.storageGb(), "storageGb");
        validatePositiveFinite(request.hoursPerDay(), "hoursPerDay");
        validatePositiveFinite(request.durationDays(), "durationDays");
    }

    private static void validateNonNegativeFinite(double value, String field) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            throw new IllegalArgumentException(field + " must be a non-negative finite number");
        }
    }

    private static void validatePositiveFinite(double value, String field) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(field + " must be a positive finite number");
        }
    }
}