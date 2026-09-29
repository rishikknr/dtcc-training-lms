package com.academy.lms.config;
import io.swagger.v3.oas.models.OpenAPI;import io.swagger.v3.oas.models.info.*;import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig {@Bean OpenAPI api(){return new OpenAPI().info(new Info().title("Academy LMS API").version("1.0.0").description("Session-authenticated REST API. Fetch /api/auth/csrf and send the X-XSRF-TOKEN header for mutations.").contact(new Contact().name("Academy Platform Team")));}}

