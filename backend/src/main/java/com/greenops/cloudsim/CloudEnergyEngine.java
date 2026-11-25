package com.greenops.cloudsim;

import org.springframework.stereotype.Component;

@Component
public class CloudEnergyEngine {

    public static final double DEFAULT_VCPU_POWER_WATTS = 10.0;
    public static final double RAM_POWER_WATTS_PER_GB = 0.375;
    public static final double NVME_STORAGE_POWER_WATTS_PER_TB = 1.2;

    public double calculateItPowerKw(
            int vcpu,
            double ramGb,
            double storageGb,
            double vcpuPowerWatts
    ) {
        validateNonNegative(vcpu, "vcpu");
        validateNonNegative(ramGb, "ramGb");
        validateNonNegative(storageGb, "storageGb");
        validatePositive(vcpuPowerWatts, "vcpuPowerWatts");

        double cpuWatts = vcpu * vcpuPowerWatts;
        double ramWatts = ramGb * RAM_POWER_WATTS_PER_GB;
        double storageWatts = (storageGb / 1000.0) * NVME_STORAGE_POWER_WATTS_PER_TB;

        return (cpuWatts + ramWatts + storageWatts) / 1000.0;
    }

    public double calculateItPowerKw(int vcpu, double ramGb, double storageGb) {
        return calculateItPowerKw(vcpu, ramGb, storageGb, DEFAULT_VCPU_POWER_WATTS);
    }

    public double calculateEnergyKwh(
            double itPowerKw,
            double pue,
            double hoursPerDay,
            double durationDays
    ) {
        validateNonNegative(itPowerKw, "itPowerKw");
        validatePositive(pue, "pue");
        validateNonNegative(hoursPerDay, "hoursPerDay");
        validateNonNegative(durationDays, "durationDays");

        return itPowerKw * pue * (hoursPerDay * durationDays);
    }

    public double calculateCarbonGrams(
            double energyKwh,
            double carbonIntensityGco2PerKwh
    ) {
        validateNonNegative(energyKwh, "energyKwh");
        validateNonNegative(carbonIntensityGco2PerKwh, "carbonIntensityGco2PerKwh");

        return energyKwh * carbonIntensityGco2PerKwh;
    }

    private void validateNonNegative(double value, String fieldName) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
            throw new IllegalArgumentException(fieldName + " must be a non-negative finite number");
        }
    }

    private void validatePositive(double value, String fieldName) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive finite number");
        }
    }
}