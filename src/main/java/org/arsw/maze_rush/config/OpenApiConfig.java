package org.arsw.maze_rush.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maze Rush API")
                        .description("API REST para el juego multijugador Maze Rush. Permite gestionar usuarios, autenticación JWT y futuras funcionalidades de juego.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ARSW - La Ley del Corazón")
                                .url("https://github.com/arsw-la-ley-del-corazon")
                                .email("support@maze-rush.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/arsw-la-ley-del-corazon/Maze-Rush-Backend/blob/main/LICENSE")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://maze-rush-api.herokuapp.com")
                                .description("Servidor de producción")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Ingresa el token JWT en el formato: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
}