package com.greenops.monitoring;

/** Implémentée par les Modules 1 (WEB) et 2 (GIT) pour permettre la ré-analyse planifiée. */
public interface MetricProvider {

    TargetType supports();

    /** Ré-analyse la cible et retourne le score/métrique courant. */
    double fetchCurrentScore(String targetId);

    /** true : plus haut = mieux (EcoIndex). false : plus haut = pire (gCO2eq). */
    boolean higherIsBetter();
}
