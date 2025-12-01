package org.arsw.maze_rush.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    public AuthController(
            AuthService authService,
            OAuth2Service oauth2Service,
            CookieUtil cookieUtil,
            UserRepository userRepository
    ) {
        this.authService = authService;
        this.oauth2Service = oauth2Service;
        this.cookieUtil = cookieUtil;
        this.userRepository = userRepository;
    }

    // Helper privado para obtener token del request
    private String extractToken(HttpServletRequest request, String cookieName) {

        return cookieUtil.getCookieValue(request, cookieName)
                .orElseGet(() -> {
                    String header = request.getHeader("Authorization");
                    if (header != null && header.startsWith("Bearer ")) {
                        return header.substring(7);
                    }
                    return null;
                });
    }

    // Login con Google
    @Operation(
            summary = "Autenticar con Google",
            description = "Autentica un usuario usando token de Google y establece cookies"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Token inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "No autorizado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> authenticateWithGoogle(
            @Valid @RequestBody OAuth2LoginRequestDTO request,
            HttpServletResponse response
    ) {
        AuthResponseDTO auth = oauth2Service.authenticateWithGoogle(request);

        cookieUtil.setAuthCookies(
                response,
                auth.getAccessToken(),
                auth.getRefreshToken(),
                auth.getExpiresIn().intValue(),
                86400
        );

        return ResponseEntity.ok(auth);
    }

    // Refresh Token
    @Operation(summary = "Renovar token de acceso",
        description = "Usa el refresh token desde cookies y genera nuevos tokens")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractToken(request, CookieUtil.REFRESH_TOKEN_COOKIE);

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken(refreshToken);

        AuthResponseDTO auth = authService.refreshToken(dto);

        cookieUtil.setAuthCookies(
                response,
                auth.getAccessToken(),
                auth.getRefreshToken(),
                auth.getExpiresIn().intValue(),
                86400
        );

        return ResponseEntity.ok(auth);
    }

    // Logout
    @Operation(summary = "Cerrar sesión",
        description = "Invalida el token actual y elimina cookies")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = extractToken(request, CookieUtil.ACCESS_TOKEN_COOKIE);

        if (token != null) {
            authService.logout(token);
        }

        cookieUtil.deleteAuthCookies(response);
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    // Validate token
    @Operation(summary = "Validar token",
        description = "Verifica si un JWT es válido")
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(HttpServletRequest request) {

        String token = extractToken(request, CookieUtil.ACCESS_TOKEN_COOKIE);

        if (token != null) {
            return ResponseEntity.ok(authService.validateToken(token));
        }

        return ResponseEntity.ok(false);
    }
    // Get user info /me
    @Operation(summary = "Obtener usuario actual",
        description = "Devuelve la info del usuario autenticado")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO.UserInfo> getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Optional<UserEntity> opt = userRepository.findByUsernameIgnoreCase(username);

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = opt.get();

        AuthResponseDTO.UserInfo dto = new AuthResponseDTO.UserInfo();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setScore(user.getScore());
        dto.setLevel(user.getLevel());

        return ResponseEntity.ok(dto);
    }
}
