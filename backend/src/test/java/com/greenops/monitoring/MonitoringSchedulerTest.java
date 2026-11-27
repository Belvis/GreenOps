package com.greenops.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringSchedulerTest {

    @Mock MonitoringSubscriptionRepository repository;
    @Mock SubscriptionCheckService checkService;
    MonitoringScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new MonitoringScheduler(repository, checkService);
    }

    private MonitoringSubscription sub(Long id, Instant lastChecked, String cron) {
        MonitoringSubscription s = new MonitoringSubscription();
        s.setId(id);
        s.setFrequencyCron(cron);
        s.setLastCheckedAt(lastChecked);
        return s;
    }

    @Test
    void neverCheckedSubscriptionIsDue() {
        assertTrue(scheduler.isDue(sub(1L, null, "0 0 8 * * *"), ZonedDateTime.now()));
    }

    @Test
    void overdueSubscriptionIsDueAndRecentOneIsNot() {
        ZonedDateTime now = ZonedDateTime.now();
        assertTrue(scheduler.isDue(
                sub(1L, now.minus(2, ChronoUnit.DAYS).toInstant(), "0 0 8 * * *"), now));
        assertFalse(scheduler.isDue(sub(2L, now.toInstant(), "0 0 8 * * *"), now));
    }

    @Test
    void invalidCronIsNeverDue() {
        assertFalse(scheduler.isDue(sub(1L, Instant.now().minusSeconds(86400), "pas un cron"),
                ZonedDateTime.now()));
    }

    @Test
    void tickDispatchesOnlyDueSubscriptions() {
        MonitoringSubscription due = sub(1L, null, "0 0 8 * * *");
        MonitoringSubscription notDue = sub(2L, Instant.now(), "0 0 8 * * *");
        when(repository.findByActiveTrue()).thenReturn(List.of(due, notDue));

        scheduler.tick();

        verify(checkService).checkAsync(1L);
        verify(checkService, never()).checkAsync(2L);
        verify(repository).save(due);
        assertNotNull(due.getLastCheckedAt());
    }
}
