package com.academy.lms.common.config;

import com.academy.lms.common.security.AccountStateFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Value("${app.cors-origins}")
  private String allowedOrigins;

  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper,
                                          AccountStateFilter accountStateFilter) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers("/api/auth/login", "/api/auth/register"))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.GET,
                "/api/courses/**", "/api/categories/**", "/api/auth/csrf",
                "/actuator/health", "/v3/api-docs/**", "/v3/api-docs",
                "/docs/**", "/docs", "/docs.html",
                "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
            .anyRequest().authenticated())
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'; object-src 'none'"))
            .frameOptions(frame -> frame.deny())
            .referrerPolicy(policy -> policy.policy(
                ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            .permissionsPolicy(policy -> policy.policy("camera=(), microphone=(), geolocation=()")))
        .sessionManagement(session -> session
            .sessionFixation(fixation -> fixation.migrateSession())
            .maximumSessions(3))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, exception) -> {
              response.setStatus(401);
              response.setContentType("application/json");
              mapper.writeValue(response.getWriter(), Map.of(
                  "status", 401, "code", "UNAUTHORIZED", "message", "Authentication required"));
            })
            .accessDeniedHandler((request, response, exception) -> {
              response.setStatus(403);
              response.setContentType("application/json");
              mapper.writeValue(response.getWriter(), Map.of(
                  "status", 403, "code", "FORBIDDEN", "message", "Access denied"));
            }))
        .addFilterAfter(accountStateFilter, SecurityContextHolderFilter.class);
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
        .map(String::trim).filter(value -> !value.isBlank()).toList());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
