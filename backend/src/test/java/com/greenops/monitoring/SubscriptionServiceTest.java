package com.greenops.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.greenops.auth.ApiException;
import com.greenops.monitoring.dto.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock MonitoringSubscriptionRepository repository;
    SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(repository);
    }

    @Test
    void createAppliesDefaultsAndOwner() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponse res = service.create(7L,
                new SubscriptionRequest(TargetType.WEB, " https://exemple.fr ", null, null));

        assertEquals("https://exemple.fr", res.targetId());
        assertEquals(10.0, res.thresholdDelta());
        assertTrue(res.active());
    }

    @Test
    void createRejectsInvalidCron() {
        ApiException ex = assertThrows(ApiException.class, () -> service.create(7L,
                new SubscriptionRequest(TargetType.GIT, "https://github.com/a/b", "nope", 5.0)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void updateOfSomeoneElsesSubscriptionIsNotFound() {
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () ->
                service.update(7L, 1L, new SubscriptionUpdateRequest(false, null, null)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateChangesActiveAndThreshold() {
        MonitoringSubscription s = new MonitoringSubscription();
        s.setTargetType(TargetType.WEB);
        s.setTargetId("x");
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(s));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        SubscriptionResponse res = service.update(7L, 1L,
                new SubscriptionUpdateRequest(false, 25.0, "0 0 12 * * *"));

        assertFalse(res.active());
        assertEquals(25.0, res.thresholdDelta());
        assertEquals("0 0 12 * * *", res.frequencyCron());
    }

    @Test
    void deleteRemovesOwnedSubscription() {
        MonitoringSubscription s = new MonitoringSubscription();
        when(repository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(s));
        service.delete(7L, 1L);
        verify(repository).delete(s);
    }
}
