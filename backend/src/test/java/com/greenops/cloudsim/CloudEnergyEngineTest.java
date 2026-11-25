package com.greenops.cloudsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CloudEnergyEngineTest {

    private CloudEnergyEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CloudEnergyEngine();
    }

    @Test
    void shouldCalculateItPowerKwAccurately() {
        // 4 vCPU * 10 W = 40 W; 16 GB * 0.375 W/GB = 6 W; 500 GB / 1000 * 1.2 W/TB = 0.6 W => 46.6 W = 0.0466 kW
        double powerKw = engine.calculateItPowerKw(4, 16.0, 500.0);
        assertEquals(0.0466, powerKw, 1e-9);
    }

    @Test
    void shouldCalculateEnergyAndCarbonAccurately() {
        double powerKw = engine.calculateItPowerKw(8, 32.0, 1000.0);
        // 80 W + 12 W + 1.2 W = 93.2 W = 0.0932 kW
        assertEquals(0.0932, powerKw, 1e-9);

        // E = 0.0932 * 1.3 * (24 * 30) = 0.0932 * 1.3 * 720 = 87.2352 kWh
        double energyKwh = engine.calculateEnergyKwh(powerKw, 1.3, 24.0, 30.0);
        assertEquals(87.2352, energyKwh, 1e-7);

        // C = 87.2352 * 56.0 = 4885.1712 gCO2eq
        double carbonGrams = engine.calculateCarbonGrams(energyKwh, 56.0);
        assertEquals(4885.1712, carbonGrams, 1e-5);
    }

    @Test
    void shouldRejectInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculateItPowerKw(-1, 8.0, 100.0));
        assertThrows(IllegalArgumentException.class, () -> engine.calculateEnergyKwh(0.1, 0.0, 10.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> engine.calculateEnergyKwh(0.1, 1.2, Double.NaN, 10.0));
        assertThrows(IllegalArgumentException.class, () -> engine.calculateCarbonGrams(-5.0, 100.0));
    }
}