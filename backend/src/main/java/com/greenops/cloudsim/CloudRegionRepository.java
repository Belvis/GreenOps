package com.greenops.cloudsim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudRegionRepository extends JpaRepository<CloudRegion, String> {

    List<CloudRegion> findByGdprStatus(GdprStatus gdprStatus);
}