# Maze Rush Backend

Backend para el juego multijugador Maze Rush desarrollado con Spring Boot con autenticación Google OAuth2.

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** - Autenticación JWT y OAuth2
- **Spring Data JPA** - Persistencia
- **PostgreSQL** - Base de datos
- **JJWT** - Manejo de tokens JWT
- **OAuth2 Client** - Autenticación con Google
- **Lombok** - Reducción de boilerplate
- **Maven** - Gestión de dependencias
- **SpringDoc OpenAPI** - Documentación API con Swagger
- **WebSockets** - Comunicación en tiempo real

## ✨ Características

- ✅ **Autenticación OAuth2 con Google** - Inicio de sesión seguro con cuentas de Google
- ✅ Sistema de JWT para tokens de acceso y refresh
- ✅ Gestión de usuarios y perfiles
- ✅ Sistema de puntuación y niveles
- ✅ WebSockets para juego multijugador en tiempo real
- ✅ API REST documentada con Swagger UI
- ✅ Validación de datos con Bean Validation
- ✅ Manejo centralizado de excepciones
- ✅ CORS configurado para desarrollo y producción

## 📋 Requisitos Previos

- Java 21 o superior
- Maven 3.8 o superior
- PostgreSQL 14 o superior
- Cuenta de Google Cloud para OAuth2 credentials

## ⚙️ Configuración

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd Maze-Rush-Backend
```

### 2. Configurar Base de Datos

Crea una base de datos PostgreSQL:

```sql
CREATE DATABASE maze_rush;
```

### 3. Configurar Variables de Entorno

Copia el archivo `.env.example` a `.env`:

```bash
cp .env.example .env
```

Edita el archivo `.env` y configura las siguientes variables:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/maze_rush
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu_password

# Google OAuth2
GOOGLE_CLIENT_ID=tu-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-google-client-secret
```

### 4. Configurar Google OAuth2

#### Paso 1: Crear Proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita **Google+ API** para tu proyecto

#### Paso 2: Crear Credenciales OAuth 2.0

1. Ve a **"APIs & Services" > "Credentials"**
2. Click en **"Create Credentials" > "OAuth 2.0 Client ID"**
3. Selecciona **"Web application"** como tipo de aplicación
4. Configura los URIs autorizados:

   **Authorized JavaScript origins:**
   ```
   http://localhost:3000
   ```

   **Authorized redirect URIs:**
   ```
   http://localhost:8080/login/oauth2/code/google
   http://localhost:3000/auth/callback
   ```

5. Copia el **Client ID** y **Client Secret** generados
6. Pégalos en tu archivo `.env`

### 5. Configurar application.properties

El archivo `application.properties` ya está configurado para usar las variables de entorno:

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
```

## 🏃 Ejecutar la Aplicación

### Desarrollo

```bash
# Usando Maven wrapper
./mvnw spring-boot:run

# O con Maven instalado
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Con Docker

```bash
docker-compose up
```

## 📖 Documentación API

Una vez la aplicación esté corriendo, accede a la documentación Swagger en:

```
http://localhost:8080/swagger-ui/index.html
```

## 🔒 Endpoints de Autenticación

### POST `/api/v1/auth/google`
Autentica un usuario con Google OAuth2 credential

**Request Body:**
```json
{
  "credential": "google-id-token"
}
```

**Response:**
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "expiresAt": "2024-01-01T12:00:00Z",
  "user": {
    "id": "uuid",
    "username": "usuario",
    "email": "usuario@gmail.com",
    "score": 0,
    "level": 1
  }
}
```

### POST `/api/v1/auth/refresh`
Renueva el token de acceso usando el refresh token

### POST `/api/v1/auth/logout`
Invalida el token actual y cierra sesión

### GET `/api/v1/auth/validate`
Valida si un token JWT es válido

### GET `/api/v1/auth/me`
Obtiene información del usuario autenticado

## 🧪 Testing

Ejecutar tests:

```bash
mvn test
```

Ejecutar tests con cobertura:

```bash
mvn test jacoco:report
```

## 📁 Estructura del Proyecto

```
src/main/java/org/arsw/maze_rush/
├── auth/                 # Autenticación y autorización
│   ├── controller/      # Controllers de auth
│   ├── service/         # Servicios de autenticación
│   ├── dto/             # DTOs de auth
│   └── util/            # Utilidades JWT y cookies
├── users/               # Gestión de usuarios
├── game/                # Lógica del juego
├── lobby/               # Gestión de lobbies
├── maze/                # Generación de laberintos
├── config/              # Configuración Spring
└── common/              # Excepciones y utilidades comunes
```

## 🔐 Seguridad

- **OAuth2**: Autenticación delegada a Google
- **JWT**: Tokens de acceso y refresh para mantener sesiones
- **BCrypt**: No se usan contraseñas tradicionales
- **CORS**: Configurado para orígenes específicos
- **Token Blacklist**: Invalidación de tokens al logout

## 🌍 Perfiles de Ejecución

- `dev` - Desarrollo local
- `test` - Testing (usa H2 in-memory)
- `prod` - Producción

Activar un perfil:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📝 Notas Importantes

- Este proyecto usa **únicamente Google OAuth2** para autenticación
- No hay registro tradicional con email/password
- Los usuarios se crean automáticamente al iniciar sesión con Google por primera vez
- El frontend debe estar configurado con el mismo Google Client ID

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver [LICENSE](LICENSE) para detalles.