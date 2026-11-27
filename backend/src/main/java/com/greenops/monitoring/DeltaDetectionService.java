package com.greenops.monitoring;

import org.springframework.stereotype.Service;

@Service
public class DeltaDetectionService {

    /** Δ (%) = ((Vt - Vt-1) / Vt-1) × 100. Si Vt-1 = 0 : 0 si Vt = 0, sinon 100. */
    public double computeDelta(double previous, double current) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }
        return ((current - previous) / previous) * 100;
    }

    /**
     * Alerte si la dégradation dépasse le seuil, ou si la note EcoIndex baisse
     * (la note ne s'applique que quand higherIsBetter = true, donc au score EcoIndex).
     */
    public DriftResult evaluate(double previous, double current, double threshold, boolean higherIsBetter) {
        double delta = computeDelta(previous, current);
        double degradation = higherIsBetter ? -delta : delta;
        boolean thresholdExceeded = degradation > threshold;
        boolean gradeDropped = higherIsBetter && gradeRank(current) > gradeRank(previous);
        return new DriftResult(delta, thresholdExceeded || gradeDropped, gradeDropped);
    }

    /** Grille EcoIndex : A (>80), B (71-80), C (56-70), D (41-55), E (26-40), F (11-25), G (<=10). */
    static int gradeRank(double score) {
        if (score > 80) return 0;
        if (score > 70) return 1;
        if (score > 55) return 2;
        if (score > 40) return 3;
        if (score > 25) return 4;
        if (score > 10) return 5;
        return 6;
    }

    public static String gradeLetter(double score) {
        return String.valueOf("ABCDEFG".charAt(gradeRank(score)));
    }
}
