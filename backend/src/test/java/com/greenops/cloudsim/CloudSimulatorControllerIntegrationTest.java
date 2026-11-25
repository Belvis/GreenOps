package com.greenops.cloudsim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CloudSimulatorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class CloudSimulatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RgpdOptimizationService optimizationService;

    @MockitoBean
    private CloudRegionRepository regionRepository;

    @MockitoBean
    private CloudSimulationRepository simulationRepository;

    @Test
    void shouldSimulateAndReturnComparison() throws Exception {
        RegionSimulationResult fr = new RegionSimulationResult(
                "FR", "France Central", "France", 1.2, GdprStatus.UE_RGPD, null,
                60.0, 0.08, 1.92, 115.2, true
        );
        SimulationComparisonResult comparison = new SimulationComparisonResult(
                0.08, 115.2, fr, 0.0, 0.0, List.of(fr)
        );
        when(optimizationService.simulate(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyBoolean()))
                .thenReturn(comparison);

        SimulateRequest req = new SimulateRequest(4, 16, 250, 24, 30, true);

        mockMvc.perform(post("/api/cloud/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedRegion.zoneCode").value("FR"))
                .andExpect(jsonPath("$.rankedRegions[0].carbonGrams").value(115.2));
    }

    @Test
    void shouldReturn400WhenFavoriteRequestHasInvalidVcpu() throws Exception {
        SaveFavoriteRequest invalidReq = new SaveFavoriteRequest(42L, "Bad", 0, 16, 250, 24, 30);

        mockMvc.perform(post("/api/cloud/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("vcpu must be positive"));
    }

    @Test
    void shouldReturn503WhenServiceThrowsIllegalStateException() throws Exception {
        when(optimizationService.simulate(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyBoolean()))
                .thenThrow(new IllegalStateException("No cloud regions configured in database"));

        SimulateRequest req = new SimulateRequest(4, 16, 250, 24, 30, true);

        mockMvc.perform(post("/api/cloud/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void shouldCreateFetchAndDeleteUserFavorite() throws Exception {
        CloudSimulation saved = new CloudSimulation(
                10L, 42L, "Prod FR", 4, 16, 250, 24, 30, true, Instant.parse("2026-11-25T14:00:00Z")
        );
        when(simulationRepository.save(any(CloudSimulation.class))).thenReturn(saved);
        when(simulationRepository.findByFavoriteTrueAndUserId(42L)).thenReturn(List.of(saved));
        when(simulationRepository.existsById(10L)).thenReturn(true);

        SaveFavoriteRequest createReq = new SaveFavoriteRequest(42L, "Prod FR", 4, 16, 250, 24, 30);

        mockMvc.perform(post("/api/cloud/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(42));

        mockMvc.perform(get("/api/cloud/favorites").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Prod FR"));

        mockMvc.perform(delete("/api/cloud/favorites/10"))
                .andExpect(status().isNoContent());
    }
}