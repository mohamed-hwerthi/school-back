package com.schoolSys.schooolSys.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Management System API")
                        .description("""
                                Multi-tenant school management system.
                                Each school has its own PostgreSQL schema.

                                **Usage:** Include the `X-Tenant-ID` header with the school's schema name
                                on all endpoints except `/api/tenants`.

                                **Authentication:** Use Bearer token (JWT) obtained from `/api/auth/login`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("School System Team")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    @Bean
    public OperationCustomizer tenantHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            String path = handlerMethod.getMethod().getDeclaringClass().getPackageName();
            if (!path.contains(".tenant")) {
                operation.addParametersItem(
                        new Parameter()
                                .in("header")
                                .name("X-Tenant-ID")
                                .description("School schema name (e.g. school_abc)")
                                .required(true)
                                .schema(new StringSchema()));
            }
            return operation;
        };
    }
}
