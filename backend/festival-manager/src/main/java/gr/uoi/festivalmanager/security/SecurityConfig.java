package gr.uoi.festivalmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // H2 console (dev)
                .requestMatchers("/h2/**").permitAll()
                // Public "view" endpoints (role-aware redaction happens in service)
                .requestMatchers(HttpMethod.GET, "/api/festivals/search-view").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/festivals/*/view").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/performances/search-view").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/performances/*/view").permitAll()
                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
