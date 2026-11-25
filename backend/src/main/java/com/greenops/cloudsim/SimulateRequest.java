package com.greenops.cloudsim;

public record SimulateRequest(
        int vcpu,
        double ramGb,
        double storageGb,
        double hoursPerDay,
        double durationDays,
        Boolean strictRgpd
) {
    public boolean effectiveStrictRgpd() {
        return strictRgpd == null || strictRgpd;
    }
}