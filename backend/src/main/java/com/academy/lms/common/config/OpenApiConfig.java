package com.academy.lms.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI academyApi() {
    return new OpenAPI().info(new Info()
        .title("Academy LMS API")
        .version("2.0.0")
        .description("Session-authenticated LMS REST API. Fetch /api/auth/csrf and send the "
            + "X-XSRF-TOKEN header on state-changing requests.")
        .contact(new Contact().name("Academy Platform Team")));
  }
}
