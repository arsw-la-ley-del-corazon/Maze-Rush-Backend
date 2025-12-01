package org.arsw.maze_rush.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void testCustomOpenAPI_Configuration() {
        //  Instanciar la clase de configuración manualmente
        OpenApiConfig config = new OpenApiConfig();

        //  Simular la inyección de @Value("${server.port}")
        String mockPort = "9090";
        ReflectionTestUtils.setField(config, "serverPort", mockPort);

        //  Ejecutar el método @Bean
        OpenAPI openAPI = config.customOpenAPI();

        //  Verificaciones Generales
        assertNotNull(openAPI, "El objeto OpenAPI no debe ser nulo");
        
        // --- Verificar Info ---
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Maze Rush API", info.getTitle());
        assertEquals("v1.0.0", info.getVersion());
        assertNotNull(info.getContact());
        assertEquals("ARSW - La Ley del Corazón", info.getContact().getName());
        assertNotNull(info.getLicense());
        assertEquals("MIT License", info.getLicense().getName());

        // --- Verificar Servers  ---
        List<Server> servers = openAPI.getServers();
        assertNotNull(servers);
        assertEquals(2, servers.size(), "Debe haber 2 servidores configurados");
        
        // Servidor Local 
        Server localServer = servers.get(0);
        assertEquals("http://localhost:" + mockPort, localServer.getUrl(), 
                "El servidor local debe usar el puerto inyectado");
        
        // Servidor Producción
        Server prodServer = servers.get(1);
        assertTrue(prodServer.getUrl().contains("azurewebsites.net"));

        // --- Verificar Seguridad ---
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());

        // Verificar Security Requirement
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey("Bearer Authentication"));
    }
}