package org.arsw.maze_rush.auth.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.AuthResponseFactory;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Set<String> tokenBlacklist;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = new HashSet<>();
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtUtil.validateToken(refreshToken) || tokenBlacklist.contains(refreshToken)) {
            throw new UnauthorizedException("Refresh token inválido");
        }

        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new UnauthorizedException("Token tipo incorrecto");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(username);
        
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Usuario no encontrado");
        }

        // Invalidar el refresh token usado
        tokenBlacklist.add(refreshToken);

        return buildAuthResponse(userOpt.get());
    }

    @Override
    public void logout(String token) {
        if (jwtUtil.validateToken(token)) {
            tokenBlacklist.add(token);
        }
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token) && !tokenBlacklist.contains(token);
    }

    private AuthResponseDTO buildAuthResponse(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        long expiresIn = jwtUtil.getAccessTokenExpiration();

        return AuthResponseFactory.build(user, accessToken, refreshToken, expiresIn);
    }
}