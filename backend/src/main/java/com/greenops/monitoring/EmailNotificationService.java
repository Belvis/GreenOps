package com.greenops.monitoring;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${greenops.mail.from:no-reply@greenops.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Échec de l'envoi de l'email à " + to, e);
        }
    }

    public void sendTestEmail(String to) {
        String html = """
            <div style="font-family:Inter,Arial,sans-serif;color:#0F172A">
              <h2 style="color:#059669">GreenOps</h2>
              <p>Cet email de test confirme que vos alertes de surveillance fonctionnent.</p>
            </div>
            """;
        sendHtml(to, "GreenOps — Email de test", html);
    }

    public void sendDriftAlert(String to, MonitoringSubscription sub,
                               double previous, double current, DriftResult result) {
        String gradeLine = result.gradeDropped()
                ? "<p><strong>Note EcoIndex :</strong> %s → %s</p>".formatted(
                        DeltaDetectionService.gradeLetter(previous),
                        DeltaDetectionService.gradeLetter(current))
                : "";
        String html = """
            <div style="font-family:Inter,Arial,sans-serif;color:#0F172A">
              <h2 style="color:#059669">GreenOps — Alerte de dérive</h2>
              <p>La ressource suivie <strong>%s</strong> (%s) s'est dégradée.</p>
              <p>Valeur précédente : <strong>%.1f</strong><br>
                 Valeur actuelle : <strong>%.1f</strong><br>
                 Variation : <strong>%+.1f %%</strong> (seuil : %.1f %%)</p>
              %s
              <p style="font-size:12px;color:#64748B">
                 Vous pouvez modifier ou désactiver cette alerte dans votre profil GreenOps.</p>
            </div>
            """.formatted(HtmlUtils.htmlEscape(sub.getTargetId()), sub.getTargetType(),
                previous, current, result.deltaPercent(), sub.getThresholdDelta(), gradeLine);
        sendHtml(to, "GreenOps — Alerte de dérive : " + sub.getTargetId(), html);
    }
}
