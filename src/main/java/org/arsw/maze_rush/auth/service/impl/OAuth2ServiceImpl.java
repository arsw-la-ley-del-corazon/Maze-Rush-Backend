package org.arsw.maze_rush.auth.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.entities.AuthProvider;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final String googleClientId;

    public OAuth2ServiceImpl(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = new RestTemplate();
        this.googleClientId = googleClientId;
    }

    @Override
    @Transactional
    public AuthResponseDTO authenticateWithGoogle(OAuth2LoginRequestDTO request) {
        try {
            // Verificar el token usando el endpoint de Google
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenInfoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new UnauthorizedException("Token de Google inválido");
            }
            
            Map<String, Object> tokenInfo = response.getBody();
            
            // Verificar que el token pertenece a nuestra aplicación
            String aud = (String) tokenInfo.get("aud");
            if (!googleClientId.equals(aud)) {
                throw new UnauthorizedException("Token de Google no válido para esta aplicación");
            }
            
            String email = (String) tokenInfo.get("email");
            String name = (String) tokenInfo.get("name");
            String providerId = (String) tokenInfo.get("sub");
            
            // email_verified puede venir como String o Boolean, necesitamos manejarlo
            Object emailVerifiedObj = tokenInfo.get("email_verified");
            boolean emailVerified = false;
            if (emailVerifiedObj instanceof Boolean) {
                emailVerified = (Boolean) emailVerifiedObj;
            } else if (emailVerifiedObj instanceof String) {
                emailVerified = "true".equalsIgnoreCase((String) emailVerifiedObj);
            }
            
            if (!emailVerified) {
                throw new UnauthorizedException("El email no está verificado en Google");
            }

            return processOAuth2User(email, name, providerId, null);

        } catch (Exception e) {
            log.error("Error al verificar token de Google", e);
            throw new UnauthorizedException("Error al validar token de Google: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO processOAuth2User(String email, String name, String providerId, String profileImageUrl) {
        // Buscar usuario por email y provider
        Optional<UserEntity> existingUser = userRepository
                .findByEmailIgnoreCaseAndAuthProvider(email.trim().toLowerCase(), AuthProvider.GOOGLE);

        UserEntity user;
        if (existingUser.isPresent()) {
            // Usuario existente - actualizar datos si es necesario
            user = existingUser.get();
            boolean updated = false;
            
            if (providerId != null && !providerId.equals(user.getProviderId())) {
                user.setProviderId(providerId);
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
            }
        } else {
            // Verificar si existe un usuario LOCAL con el mismo email
            Optional<UserEntity> localUser = userRepository
                    .findByEmailIgnoreCaseAndAuthProvider(email.trim().toLowerCase(), AuthProvider.LOCAL);
            
            if (localUser.isPresent()) {
                throw new UnauthorizedException(
                    "Ya existe una cuenta local con este email. Por favor inicie sesión con email y contraseña.");
            }
            
            // Crear nuevo usuario OAuth2
            user = new UserEntity();
            user.setEmail(email.trim().toLowerCase());
            user.setUsername(generateUniqueUsername(name, email));
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            // password es null para usuarios OAuth2
            user = userRepository.save(user);
        }

        return buildAuthResponse(user);
    }

    /**
     * Genera un username único basado en el nombre y email del usuario
     */
    private String generateUniqueUsername(String name, String email) {
        // Primero intentar con el nombre
        if (name != null && !name.isBlank()) {
            String baseUsername = name.trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            
            if (!baseUsername.isEmpty() && !userRepository.existsByUsernameIgnoreCase(baseUsername)) {
                return baseUsername;
            }
            
            // Si ya existe, agregar números
            for (int i = 1; i <= 100; i++) {
                String username = baseUsername + i;
                if (!userRepository.existsByUsernameIgnoreCase(username)) {
                    return username;
                }
            }
        }
        
        // Si no funciona con el nombre, usar la parte local del email
        String localPart = email.substring(0, email.indexOf('@'))
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        
        if (!userRepository.existsByUsernameIgnoreCase(localPart)) {
            return localPart;
        }
        
        // Agregar números al email
        for (int i = 1; i <= 1000; i++) {
            String username = localPart + i;
            if (!userRepository.existsByUsernameIgnoreCase(username)) {
                return username;
            }
        }
        
        // Último recurso: usar timestamp
        return localPart + System.currentTimeMillis();
    }

    private AuthResponseDTO buildAuthResponse(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        response.setExpiresAt(Instant.now().plus(jwtUtil.getAccessTokenExpiration(), ChronoUnit.SECONDS));
        
        // Usuario básico para evitar duplicación
        AuthResponseDTO.UserInfo userInfo = new AuthResponseDTO.UserInfo();
        userInfo.setId(user.getId().toString());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setScore(user.getScore());
        userInfo.setLevel(user.getLevel());
        response.setUser(userInfo);
        
        return response;
    }
}
