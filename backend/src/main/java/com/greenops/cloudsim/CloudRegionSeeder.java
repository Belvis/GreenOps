package com.greenops.cloudsim;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CloudRegionSeeder implements CommandLineRunner {

    private final CloudRegionRepository regionRepository;

    public CloudRegionSeeder(CloudRegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public void run(String... args) {
        List<CloudRegion> defaults = List.of(
                new CloudRegion("FR", "France Central", "France", 1.25, GdprStatus.UE_RGPD, "Hébergement dans l'UE soumis au RGPD"),
                new CloudRegion("DE", "Germany Central", "Germany", 1.22, GdprStatus.UE_RGPD, "Hébergement dans l'UE soumis au RGPD"),
                new CloudRegion("SE", "Sweden North", "Sweden", 1.15, GdprStatus.UE_RGPD, "Hébergement dans l'UE soumis au RGPD, forte part hydroélectrique"),
                new CloudRegion("CH", "Switzerland North", "Switzerland", 1.18, GdprStatus.ADEQUAT, "Décision d'adéquation RGPD de la Commission européenne"),
                new CloudRegion("US-EAST-1", "US East (N. Virginia)", "United States", 1.20, GdprStatus.HORS_UE_CLOUD_ACT, "Hors UE, exposé au US CLOUD Act et transferts encadrés")
        );

        for (CloudRegion region : defaults) {
            regionRepository.findById(region.getZoneCode()).ifPresentOrElse(
                    existing -> {
                        existing.setDisplayName(region.getDisplayName());
                        existing.setCountry(region.getCountry());
                        existing.setPue(region.getPue());
                        existing.setGdprStatus(region.getGdprStatus());
                        existing.setLegalNote(region.getLegalNote());
                        regionRepository.save(existing);
                    },
                    () -> regionRepository.save(region)
            );
        }
    }
}