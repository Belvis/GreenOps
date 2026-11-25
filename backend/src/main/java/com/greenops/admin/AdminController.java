package com.greenops.admin;

import com.greenops.auth.Role;
import com.greenops.auth.User;
import com.greenops.auth.UserRepository;
import com.greenops.auth.UserStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final SystemCoefficientRepository coefficientRepository;

    public AdminController(UserRepository userRepository, SystemCoefficientRepository coefficientRepository) {
        this.userRepository = userRepository;
        this.coefficientRepository = coefficientRepository;
    }

    @PostConstruct
    public void initDefaultCoefficients() {
        if (coefficientRepository.count() == 0) {
            coefficientRepository.saveAll(List.of(
                    new SystemCoefficient("DEFAULT_PUE", 1.20, "ratio", "PUE moyen des datacenters CI/CD"),
                    new SystemCoefficient("RUNNER_POWER_LINUX_KW", 0.025, "kW", "Puissance électrique d'un runner Linux 2-vCPU"),
                    new SystemCoefficient("CARBON_INTENSITY_CI_AVG", 300.0, "gCO2eq/kWh", "Intensité carbone moyenne des runners hébergés")
            ));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<?> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "role", u.getRole().name(),
                        "status", u.getStatus().name(),
                        "active", u.getStatus() == UserStatus.ACTIVE
                ))
                .toList();
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            String rawRole = body.get("role");
            user.setRole(Role.valueOf(rawRole));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Rôle mis à jour avec succès"));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return userRepository.findById(id).map(user -> {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            user.setStatus(active ? UserStatus.ACTIVE : UserStatus.SUSPENDED);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Statut du compte mis à jour"));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/quotas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<?> getApiQuotas() {
        return List.of(
                Map.of("provider", "Google PageSpeed Insights", "usedToday", 14, "dailyLimit", 25000),
                Map.of("provider", "The Green Web Foundation", "usedToday", 22, "dailyLimit", 10000),
                Map.of("provider", "Electricity Maps API", "usedToday", 38, "dailyLimit", 5000)
        );
    }

    @GetMapping("/coefficients")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SystemCoefficient> getCoefficients() {
        return coefficientRepository.findAll();
    }

    @PutMapping("/coefficients/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCoefficient(@PathVariable String key, @RequestBody Map<String, Double> body) {
        Double newValue = body.get("paramValue");
        if (newValue == null || newValue <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "La valeur du coefficient doit être strictement positive."));
        }
        return coefficientRepository.findByParamKey(key).map(coeff -> {
            coeff.setParamValue(newValue);
            coefficientRepository.save(coeff);
            return ResponseEntity.ok(coeff);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}