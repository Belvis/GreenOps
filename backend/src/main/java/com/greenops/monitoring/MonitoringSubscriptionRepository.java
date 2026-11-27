package com.greenops.monitoring;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoringSubscriptionRepository extends JpaRepository<MonitoringSubscription, Long> {
    List<MonitoringSubscription> findByUserId(Long userId);
    List<MonitoringSubscription> findByActiveTrue();
    Optional<MonitoringSubscription> findByIdAndUserId(Long id, Long userId);
}
