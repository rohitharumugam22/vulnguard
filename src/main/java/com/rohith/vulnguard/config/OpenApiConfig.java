package com.rohith.vulnguard.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI vulnGuardOpenAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info buildInfo() {
        return new Info()
                .title("VulnGuard - Attack Surface Management Simulator")
                .description("""
                        ## VulnGuard API
                        Intelligent Attack Surface Management Simulator.
                        
                        ### Quick Start
                        1. POST /api/auth/register - create account
                        2. POST /api/auth/login - get JWT token
                        3. Click Authorize - paste Bearer token
                        4. Create assets, trigger scans, view dashboard, export reports
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Rohith").email("rohith@vulnguard.io"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token (without Bearer prefix)");
    }
}
