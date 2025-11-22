package org.arsw.maze_rush.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.arsw.maze_rush.common.ApiError;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Autenticación", description = "Endpoints para autenticación con Google OAuth2 y gestión de tokens JWT mediante cookies")
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oauth2Service;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, OAuth2Service oauth2Service, 
                         CookieUtil cookieUtil, UserRepository userRepository) {
        this.authService = authService;
        this.oauth2Service = oauth2Service;
        this.cookieUtil = cookieUtil;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Autenticar con Google",
        description = "Autentica un usuario usando un token de ID de Google y establece cookies"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Autenticación exitosa",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Token de Google inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token no verificado o inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> authenticateWithGoogle(
        @Parameter(description = "Token de ID de Google", required = true)
        @Valid @RequestBody OAuth2LoginRequestDTO request,
        HttpServletResponse response
    ) {
        AuthResponseDTO authResponse = oauth2Service.authenticateWithGoogle(request);
        
        // Establecer cookies con los tokens
        cookieUtil.setAuthCookies(
            response,
            authResponse.getAccessToken(),
            authResponse.getRefreshToken(),
            authResponse.getExpiresIn().intValue(),
            86400 // 24 horas para refresh token
        );
        
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
        summary = "Renovar token de acceso",
        description = "Genera un nuevo token de acceso usando el refresh token desde cookies"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Token renovado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Refresh token inválido o faltante",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Refresh token expirado o inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Intentar obtener el refresh token desde cookies
        String refreshToken = cookieUtil.getCookieValue(request, CookieUtil.REFRESH_TOKEN_COOKIE)
                .orElse(null);
        
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(refreshToken);
        
        AuthResponseDTO authResponse = authService.refreshToken(refreshRequest);
        
        // Actualizar cookies con los nuevos tokens
        cookieUtil.setAuthCookies(
            response,
            authResponse.getAccessToken(),
            authResponse.getRefreshToken(),
            authResponse.getExpiresIn().intValue(),
            86400 // 24 horas para refresh token
        );
        
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida el token de acceso actual y elimina las cookies de autenticación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Sesión cerrada exitosamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Token de autorización faltante o inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Intentar obtener el token desde cookies o header
        String token = cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)
                .orElse(null);
        
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        if (token != null) {
            authService.logout(token);
        }
        
        // Eliminar cookies de autenticación
        cookieUtil.deleteAuthCookies(response);
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Validar token",
        description = "Valida si un token JWT desde cookies es válido y activo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estado del token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "boolean", example = "true")
            )
        )
    })
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(HttpServletRequest request) {
        // Intentar obtener el token desde cookies o header
        String token = cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)
                .orElse(null);
        
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        if (token != null) {
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(isValid);
        }
        
        return ResponseEntity.ok(false);
    }

    @Operation(
        summary = "Obtener usuario actual",
        description = "Obtiene la información del usuario autenticado desde el token en cookies"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.UserInfo.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO.UserInfo> getCurrentUser() {
        // Obtener el username del contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Buscar el usuario completo en la base de datos
        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserEntity user = userOpt.get();
        
        // Construir la respuesta con información completa
        AuthResponseDTO.UserInfo userInfo = new AuthResponseDTO.UserInfo();
        userInfo.setId(user.getId().toString());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setScore(user.getScore());
        userInfo.setLevel(user.getLevel());
        
        return ResponseEntity.ok(userInfo);
    }
}