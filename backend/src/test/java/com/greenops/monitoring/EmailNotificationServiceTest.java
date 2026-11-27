
package com.greenops.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock JavaMailSender mailSender;
    EmailNotificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailNotificationService(mailSender, "no-reply@greenops.local");
        lenient().when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session) null));
    }

    private MimeMessage captureSent() {
        ArgumentCaptor<MimeMessage> captor =
                ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void testEmailGoesToRecipientWithExpectedSubject() throws Exception {
        service.sendTestEmail("user@greenops.fr");

        MimeMessage sent = captureSent();
        assertEquals("user@greenops.fr",
                sent.getRecipients(Message.RecipientType.TO)[0].toString());
        assertTrue(sent.getSubject().contains("Email de test"));
    }

    @Test
    void driftAlertContainsTargetInSubjectAndGoesToUser() throws Exception {
        MonitoringSubscription sub = new MonitoringSubscription();
        sub.setTargetType(TargetType.WEB);
        sub.setTargetId("https://exemple.fr");
        sub.setThresholdDelta(10);
        DriftResult result = new DriftResult(-25.0, true, true);

        service.sendDriftAlert("user@greenops.fr", sub, 80, 60, result);

        MimeMessage sent = captureSent();
        assertEquals("user@greenops.fr",
                sent.getRecipients(Message.RecipientType.TO)[0].toString());
        assertTrue(sent.getSubject().contains("Alerte"));
        assertTrue(sent.getSubject().contains("https://exemple.fr"));
    }

    @Test
    void driftAlertWithoutGradeDropDoesNotContainGradeLine() throws Exception {
        MonitoringSubscription sub = new MonitoringSubscription();
        sub.setTargetType(TargetType.WEB);
        sub.setTargetId("https://exemple.fr");
        sub.setThresholdDelta(10);
        DriftResult result = new DriftResult(-25.0, true, false);

        service.sendDriftAlert("user@greenops.fr", sub, 80, 60, result);

        MimeMessage sent = captureSent();
        assertFalse(sent.getContent().toString().contains("Note EcoIndex"));
    }

    @Test
    void messagingExceptionIsWrappedInIllegalStateException() throws Exception {
        MimeMessage brokenMessage = mock(MimeMessage.class);

        doThrow(new MessagingException("Erreur de préparation"))
                .when(brokenMessage).setFrom(any(Address.class));

        when(mailSender.createMimeMessage()).thenReturn(brokenMessage);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.sendTestEmail("user@greenops.fr")
        );

        assertTrue(exception.getMessage().contains("user@greenops.fr"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
