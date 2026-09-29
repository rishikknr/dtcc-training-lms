package com.academy.lms.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
  @Bean SecurityFilterChain security(HttpSecurity http,ObjectMapper mapper) throws Exception {
    http.cors(c->c.configurationSource(cors())).csrf(c->c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
      .authorizeHttpRequests(a->a
        .requestMatchers(HttpMethod.GET,"/api/courses/**","/api/categories/**","/api/auth/csrf","/actuator/health","/v3/api-docs/**","/docs/**","/docs.html").permitAll()
        .requestMatchers(HttpMethod.POST,"/api/auth/register","/api/auth/login").permitAll()
        .anyRequest().authenticated())
      .headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
        .frameOptions(f->f.deny()).referrerPolicy(r->r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
      .sessionManagement(s->s.sessionFixation(f->f.migrateSession()).maximumSessions(3))
      .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setStatus(401);res.setContentType("application/json");mapper.writeValue(res.getWriter(),Map.of("status",401,"code","UNAUTHORIZED","message","Authentication required"));})
        .accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json");mapper.writeValue(res.getWriter(),Map.of("status",403,"code","FORBIDDEN","message","Access denied"));}));
    return http.build();
  }
  @Value("${app.cors-origins}") private String allowedOrigins;
  @Bean CorsConfigurationSource cors(){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Content-Type","X-XSRF-TOKEN"));c.setAllowCredentials(true);c.setMaxAge(3600L);UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/api/**",c);return s;}
}

