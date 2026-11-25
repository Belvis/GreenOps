package com.greenops.cloudsim;

public record SaveFavoriteRequest(
        Long userId,
        String name,
        int vcpu,
        double ramGb,
        double storageGb,
        double hoursPerDay,
        double durationDays
) {
}