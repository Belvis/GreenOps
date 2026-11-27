package com.greenops.monitoring;

import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
public class MonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonitoringScheduler.class);

    private final MonitoringSubscriptionRepository repository;
    private final SubscriptionCheckService checkService;

    public MonitoringScheduler(MonitoringSubscriptionRepository repository,
                               SubscriptionCheckService checkService) {
        this.repository = repository;
        this.checkService = checkService;
    }

    /** Tick chaque minute ; chaque abonnement a sa propre fréquence (frequency_cron). */
    @Scheduled(cron = "${greenops.monitoring.tick-cron:0 * * * * *}")
    public void tick() {
        ZonedDateTime now = ZonedDateTime.now();
        for (MonitoringSubscription sub : repository.findByActiveTrue()) {
            if (isDue(sub, now)) {
                // On marque tout de suite pour éviter un double traitement au tick suivant
                sub.setLastCheckedAt(now.toInstant());
                repository.save(sub);
                checkService.checkAsync(sub.getId());
            }
        }
    }

    boolean isDue(MonitoringSubscription sub, ZonedDateTime now) {
        if (sub.getLastCheckedAt() == null) {
            return true;
        }
        try {
            ZonedDateTime next = CronExpression.parse(sub.getFrequencyCron())
                    .next(sub.getLastCheckedAt().atZone(now.getZone()));
            return next != null && !next.isAfter(now);
        } catch (IllegalArgumentException e) {
            log.warn("Cron invalide pour l'abonnement {} : {}", sub.getId(), sub.getFrequencyCron());
            return false;
        }
    }
}
