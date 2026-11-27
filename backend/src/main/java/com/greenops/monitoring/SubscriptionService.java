package com.greenops.monitoring;

import com.greenops.auth.ApiException;
import com.greenops.monitoring.dto.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final MonitoringSubscriptionRepository repository;

    public SubscriptionService(MonitoringSubscriptionRepository repository) {
        this.repository = repository;
    }

    public List<SubscriptionResponse> list(Long userId) {
        return repository.findByUserId(userId).stream().map(SubscriptionResponse::from).toList();
    }

    public SubscriptionResponse create(Long userId, SubscriptionRequest req) {
        MonitoringSubscription sub = new MonitoringSubscription();
        sub.setUserId(userId);
        sub.setTargetType(req.targetType());
        sub.setTargetId(req.targetId().trim());
        if (req.frequencyCron() != null && !req.frequencyCron().isBlank()) {
            checkCron(req.frequencyCron());
            sub.setFrequencyCron(req.frequencyCron());
        }
        if (req.thresholdDelta() != null) {
            sub.setThresholdDelta(req.thresholdDelta());
        }
        return SubscriptionResponse.from(repository.save(sub));
    }

    public SubscriptionResponse update(Long userId, Long id, SubscriptionUpdateRequest req) {
        MonitoringSubscription sub = find(userId, id);
        if (req.active() != null) sub.setActive(req.active());
        if (req.thresholdDelta() != null) sub.setThresholdDelta(req.thresholdDelta());
        if (req.frequencyCron() != null) {
            checkCron(req.frequencyCron());
            sub.setFrequencyCron(req.frequencyCron());
        }
        return SubscriptionResponse.from(repository.save(sub));
    }

    public void delete(Long userId, Long id) {
        repository.delete(find(userId, id));
    }

    private MonitoringSubscription find(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Abonnement introuvable"));
    }

    private void checkCron(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expression cron invalide : " + cron);
        }
    }
}
