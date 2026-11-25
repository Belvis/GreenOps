package com.greenops.cloudsim;

public enum GdprStatus {
    UE_RGPD,
    ADEQUAT,
    HORS_UE_CLOUD_ACT;

    public boolean isEuOrEee() {
        return this == UE_RGPD;
    }

    public boolean isCompliantInStrictMode() {
        return this == UE_RGPD || this == ADEQUAT;
    }
}