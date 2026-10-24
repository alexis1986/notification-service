package com.alexdevvv.notifications.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documento OpenAPI servido en /v3/api-docs y Swagger UI en /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME = "ApiKey";

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("v1")
                        .description("""
                                Envío multi-canal y multi-tenant de notificaciones (EMAIL, WHATSAPP, PUSH).
                                La entrega es asíncrona: el POST encola y el estado se consulta por id.
                                """))
                .components(new Components().addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Identifica al cliente (tenant)")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
    }
}
