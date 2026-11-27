package com.greenops.monitoring;

import com.greenops.auth.UserRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionCheckService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCheckService.class);

    private final MonitoringSubscriptionRepository repository;
    private final UserRepository userRepository;
    private final Map<TargetType, MetricProvider> providers = new EnumMap<>(TargetType.class);
    private final DeltaDetectionService detectionService;
    private final EmailNotificationService notificationService;

    public SubscriptionCheckService(MonitoringSubscriptionRepository repository,
                                    UserRepository userRepository,
                                    List<MetricProvider> providerList,
                                    DeltaDetectionService detectionService,
                                    EmailNotificationService notificationService) {
        this.repository = repository;
        this.userRepository = userRepository;
        providerList.forEach(p -> providers.put(p.supports(), p));
        this.detectionService = detectionService;
        this.notificationService = notificationService;
    }

    @Async
    public void checkAsync(Long subscriptionId) {
        check(subscriptionId);
    }

    public void check(Long subscriptionId) {
        MonitoringSubscription sub = repository.findById(subscriptionId).orElse(null);
        if (sub == null || !sub.isActive()) {
            return;
        }
        MetricProvider provider = providers.get(sub.getTargetType());
        if (provider == null) {
            log.warn("Aucun MetricProvider pour le type {}", sub.getTargetType());
            return;
        }

        double current;
        try {
            current = provider.fetchCurrentScore(sub.getTargetId());
        } catch (RuntimeException e) {
            log.warn("Ré-analyse impossible pour l'abonnement {} : {}", subscriptionId, e.getMessage());
            return;
        }

        Double previous = sub.getLastScore();
        if (previous != null) {
            DriftResult result = detectionService.evaluate(
                    previous, current, sub.getThresholdDelta(), provider.higherIsBetter());
            if (result.alert()) {
                userRepository.findById(sub.getUserId()).ifPresent(user ->
                        notificationService.sendDriftAlert(
                                user.getEmail(), sub, previous, current, result));
            }
        }
        sub.setLastScore(current);
        repository.save(sub);
    }
}
