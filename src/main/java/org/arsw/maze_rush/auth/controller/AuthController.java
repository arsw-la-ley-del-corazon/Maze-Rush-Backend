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
import org.arsw.maze_rush.auth.dto.LoginRequestDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.common.ApiError;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea un nuevo usuario en el sistema y retorna tokens de acceso"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Usuario registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "El username o email ya están en uso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
        @Parameter(description = "Datos del usuario a registrar", required = true)
        @Valid @RequestBody UserRequestDTO request
    ) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario con email/username y contraseña"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
        @Parameter(description = "Credenciales de acceso (email o username)", required = true)
        @Valid @RequestBody LoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "Renovar token de acceso",
        description = "Genera un nuevo token de acceso usando el refresh token"
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
        @Parameter(description = "Token de refresco", required = true)
        @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida el token de acceso actual (logout)"
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
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Validar token",
        description = "Valida si un token JWT es válido y activo"
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
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(isValid);
        }
        return ResponseEntity.ok(false);
    }
}