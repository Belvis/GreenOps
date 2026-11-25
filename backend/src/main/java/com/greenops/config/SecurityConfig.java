
package com.greenops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // On conserve la protection CSRF des autres fonctionnalités.
            // Seul l'endpoint d'audit est exclu.
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/api/web-audit/**"
            ))

            .authorizeHttpRequests(auth -> auth
                // Autoriser les vérifications CORS du navigateur.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autoriser l'analyse Web sans connexion.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/web-audit/analyze"
                ).permitAll()

                // Les autres routes restent protégées.
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
