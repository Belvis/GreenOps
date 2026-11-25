package com.greenops.auth;

import com.greenops.auth.dto.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final long RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final String frontendUrl;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService,
                       @org.springframework.beans.factory.annotation.Value(
                               "${greenops.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email");
        }
        boolean isFirstUser = userRepository.count() == 0;
        User user = new User();
        user.setEmail(req.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(isFirstUser ? Role.ROLE_ADMIN : Role.ROLE_USER);
        user.setStatus(isFirstUser ? UserStatus.ACTIVE : UserStatus.PENDING);
        userRepository.save(user);

        if (isFirstUser) {
            return new MessageResponse("Compte Super-Admin initial créé et activé.");
        }
        return new MessageResponse("Compte créé. Il sera actif après validation par un administrateur.");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        if (user.getStatus() == UserStatus.PENDING) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Compte en attente de validation");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Compte suspendu");
        }
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                user.getEmail(), user.getRole().name());
    }

    /** Réponse identique que l'email existe ou non (pas de fuite d'information). */
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmailIgnoreCase(req.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            tokenRepository.save(new PasswordResetToken(token, user,
                    Instant.now().plus(RESET_TOKEN_MINUTES, ChronoUnit.MINUTES)));
            emailService.sendPasswordReset(user.getEmail(),
                    frontendUrl + "/reset-password?token=" + token);
        });
        return new MessageResponse("Si ce compte existe, un email de réinitialisation a été envoyé.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        PasswordResetToken prt = tokenRepository.findByToken(req.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Lien invalide"));
        if (prt.isUsed() || prt.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Lien expiré ou déjà utilisé");
        }
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        prt.setUsed(true);
        tokenRepository.save(prt);
        return new MessageResponse("Mot de passe mis à jour.");
    }
}