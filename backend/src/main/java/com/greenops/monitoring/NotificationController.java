package com.greenops.monitoring;

import com.greenops.auth.User;
import com.greenops.auth.dto.MessageResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailNotificationService emailService;

    public NotificationController(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test-email")
    public MessageResponse testEmail(@AuthenticationPrincipal User user) {
        emailService.sendTestEmail(user.getEmail());
        return new MessageResponse("Email de test envoyé à " + user.getEmail());
    }
}
