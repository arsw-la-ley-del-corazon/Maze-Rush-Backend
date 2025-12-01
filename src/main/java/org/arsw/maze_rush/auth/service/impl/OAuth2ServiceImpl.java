package org.arsw.maze_rush.auth.service.impl;

import java.util.Map;
import java.util.Optional;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.AuthResponseFactory;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.entities.AuthProvider;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final String googleClientId;
    private OAuth2Service self;

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
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenInfoUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new UnauthorizedException("Token de Google inválido");
            }

            Map<String, Object> tokenInfo = response.getBody();

            if (!googleClientId.equals(tokenInfo.get("aud"))) {
                throw new UnauthorizedException("Token de Google no válido para esta aplicación");
            }

            String email = (String) tokenInfo.get("email");
            String name = (String) tokenInfo.get("name");
            String providerId = (String) tokenInfo.get("sub");

            boolean emailVerified = false;
            Object emailVerifiedObj = tokenInfo.get("email_verified");

            if (emailVerifiedObj instanceof Boolean b) emailVerified = b;
            if (emailVerifiedObj instanceof String s) emailVerified = "true".equalsIgnoreCase(s);

            if (!emailVerified) {
                throw new UnauthorizedException("El email no está verificado en Google");
            }
            return self.processOAuth2User(email, name, providerId, null);

        } catch (Exception e) {
            log.error("Error al verificar token de Google", e);
            if (e instanceof UnauthorizedException authEx) {
                throw authEx;
            }
            throw new UnauthorizedException("Error al validar token de Google: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO processOAuth2User(String email, String name, String providerId, String profileImageUrl) {
        Optional<UserEntity> existingUser = userRepository
                .findByEmailIgnoreCaseAndAuthProvider(email.toLowerCase(), AuthProvider.GOOGLE);

        UserEntity user;

        if (existingUser.isPresent()) {
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
            Optional<UserEntity> localUser = userRepository
                    .findByEmailIgnoreCaseAndAuthProvider(email.toLowerCase(), AuthProvider.LOCAL);

            if (localUser.isPresent()) {
                throw new UnauthorizedException("Ya existe una cuenta local con este email. Use email/contraseña.");
            }

            user = new UserEntity();
            user.setEmail(email.trim().toLowerCase());
            user.setUsername(generateUniqueUsername(name, email));
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);

            user = userRepository.save(user);
        }

        return buildAuthResponse(user);
    }

    private String generateUniqueUsername(String name, String email) {
        if (name != null && !name.isBlank()) {
            String base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (!base.isBlank() && !userRepository.existsByUsernameIgnoreCase(base)) {
                return base;
            }
            for (int i = 1; i <= 100; i++) {
                String candidate = base + i;
                if (!userRepository.existsByUsernameIgnoreCase(candidate)) {
                    return candidate;
                }
            }
        }
        String local = email.substring(0, email.indexOf("@")).toLowerCase().replaceAll("[^a-z0-9]", "");
        if (!userRepository.existsByUsernameIgnoreCase(local)) {
            return local;
        }
        for (int i = 1; i <= 1000; i++) {
            String candidate = local + i;
            if (!userRepository.existsByUsernameIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return local + System.currentTimeMillis();
    }

    private AuthResponseDTO buildAuthResponse(UserEntity user) {
        return AuthResponseFactory.build(
                user,
                jwtUtil.generateAccessToken(user.getUsername()),
                jwtUtil.generateRefreshToken(user.getUsername()),
                jwtUtil.getAccessTokenExpiration()
        );
    }
}