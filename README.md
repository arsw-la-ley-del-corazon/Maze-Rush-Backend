# Maze Rush Backend

Backend para el juego multijugador Maze Rush desarrollado con Spring Boot.

## Tecnologías

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** - Autenticación JWT y OAuth2
- **Spring Data JPA** - Persistencia
- **PostgreSQL** - Base de datos
- **BCrypt** - Encriptación de contraseñas
- **JJWT** - Manejo de tokens JWT
- **OAuth2 Client** - Autenticación con Google
- **Lombok** - Reducción de boilerplate
- **Maven** - Gestión de dependencias
- **SpringDoc OpenAPI** - Documentación API con Swagger

## Características

- ✅ Autenticación tradicional con JWT (email/contraseña)
- ✅ Autenticación OAuth2 con Google
- ✅ Gestión de usuarios y perfiles
- ✅ Sistema de puntuación y niveles
- ✅ API REST documentada con Swagger UI
- ✅ Validación de datos con Bean Validation
- ✅ Manejo centralizado de excepciones
- ✅ CORS configurado para desarrollo y producción

## Configuración OAuth2

Para habilitar la autenticación con Google, consulta la guía completa en [OAUTH2_SETUP.md](OAUTH2_SETUP.md).

**Resumen rápido:**
1. Crear proyecto en [Google Cloud Console](https://console.cloud.google.com/)
2. Configurar OAuth2 credentials
3. Agregar Client ID y Secret en `application-dev.properties`
4. Configurar redirect URIs autorizadas

## Ejecución

### Desarrollo
```bash
mvn spring-boot:run
```

### Con Docker
```bash
docker-compose up
```

## Licencia

Este proyecto está bajo la Licencia MIT - ver [LICENSE](LICENSE) para detalles.