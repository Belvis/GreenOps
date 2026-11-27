package com.greenops.monitoring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.greenops.auth.User;
import com.greenops.auth.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionCheckServiceTest {

    @Mock MonitoringSubscriptionRepository repository;
    @Mock UserRepository userRepository;
    @Mock MetricProvider provider;
    @Mock EmailNotificationService notificationService;

    SubscriptionCheckService service;

    @BeforeEach
    void setUp() {
        when(provider.supports()).thenReturn(TargetType.WEB);
        service = new SubscriptionCheckService(repository, userRepository, List.of(provider),
                new DeltaDetectionService(), notificationService);
    }

    private MonitoringSubscription sub(Double lastScore) {
        MonitoringSubscription s = new MonitoringSubscription();
        s.setId(1L);
        s.setUserId(7L);
        s.setTargetType(TargetType.WEB);
        s.setTargetId("https://exemple.fr");
        s.setThresholdDelta(10);
        s.setLastScore(lastScore);
        return s;
    }

    @Test
    void firstCheckStoresBaselineWithoutEmail() {
        MonitoringSubscription s = sub(null);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(provider.fetchCurrentScore("https://exemple.fr")).thenReturn(80.0);

        service.check(1L);

        assertEquals(80.0, s.getLastScore());
        verify(repository).save(s);
        verifyNoInteractions(notificationService);
    }

    @Test
    void degradationSendsEmailToOwner() {
        MonitoringSubscription s = sub(80.0);
        User user = new User();
        user.setEmail("a@b.fr");
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(provider.fetchCurrentScore("https://exemple.fr")).thenReturn(60.0);
        when(provider.higherIsBetter()).thenReturn(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        service.check(1L);

        verify(notificationService).sendDriftAlert(eq("a@b.fr"), eq(s), eq(80.0), eq(60.0), any());
        assertEquals(60.0, s.getLastScore());
    }

    @Test
    void noEmailBelowThreshold() {
        MonitoringSubscription s = sub(80.0);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(provider.fetchCurrentScore("https://exemple.fr")).thenReturn(78.0);
        when(provider.higherIsBetter()).thenReturn(true);

        service.check(1L);

        verifyNoInteractions(notificationService);
        verify(repository).save(s);
    }

    @Test
    void providerFailureLeavesSubscriptionUntouched() {
        MonitoringSubscription s = sub(80.0);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        when(provider.fetchCurrentScore(any())).thenThrow(new RuntimeException("API down"));

        service.check(1L);

        verify(repository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void inactiveOrMissingSubscriptionIsSkipped() {
        MonitoringSubscription s = sub(80.0);
        s.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        service.check(1L);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        service.check(2L);

        verify(provider, never()).fetchCurrentScore(any());
    }
}
