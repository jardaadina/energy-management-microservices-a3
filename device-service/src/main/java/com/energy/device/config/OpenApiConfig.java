package com.energy.device.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviceServiceOpenAPI() {
        Server traefikServer = new Server();
        traefikServer.setUrl("http://localhost/devices");
        traefikServer.setDescription("Traefik Gateway - Devices");

        Server traefikUserDevicesServer = new Server();
        traefikUserDevicesServer.setUrl("http://localhost/user-devices");
        traefikUserDevicesServer.setDescription("Traefik Gateway - User Devices");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8082");
        localServer.setDescription("Local Development Server");

        Info info = new Info()
                .title("Device Management API")
                .version("1.0")
                .description("API pentru gestionarea dispozitivelor")
                .contact(new Contact()
                        .name("Your Name")
                        .email("your.email@example.com"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(traefikServer, traefikUserDevicesServer, localServer))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}