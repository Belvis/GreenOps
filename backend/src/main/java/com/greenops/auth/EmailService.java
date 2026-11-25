package com.greenops.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Service d'envoi d'emails HTML, réutilisé par le Module 4 (alertes). */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender,
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

    public void sendPasswordReset(String to, String resetLink) {
        String html = """
            <div style="font-family:Inter,Arial,sans-serif;color:#0F172A">
              <h2 style="color:#059669">GreenOps</h2>
              <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
              <p><a href="%s" style="background:#10B981;color:#fff;padding:10px 16px;
                 border-radius:8px;text-decoration:none">Choisir un nouveau mot de passe</a></p>
              <p style="font-size:12px;color:#64748B">Ce lien expire dans 30 minutes.
                 Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
            </div>
            """.formatted(resetLink);
        sendHtml(to, "GreenOps — Réinitialisation de mot de passe", html);
    }
}
