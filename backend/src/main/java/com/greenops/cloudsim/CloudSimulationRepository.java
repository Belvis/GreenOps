package com.greenops.cloudsim;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudSimulationRepository extends JpaRepository<CloudSimulation, Long> {

    List<CloudSimulation> findByFavoriteTrue();

    List<CloudSimulation> findByFavoriteTrueAndUserId(Long userId);

    List<CloudSimulation> findByUserId(Long userId);
}