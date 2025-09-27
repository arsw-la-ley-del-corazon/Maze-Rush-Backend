package org.arsw.maze_rush.auth.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.LoginRequestDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.ConflictException;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Set<String> tokenBlacklist; // En producción usar Redis

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = new HashSet<>();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        UserEntity user = findUserByEmailOrUsername(request);
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(UserRequestDTO request) {
        // Validar unicidad
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("El username ya está en uso");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El email ya está en uso");
        }

        // Crear usuario
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        return buildAuthResponse(user);
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

    private UserEntity findUserByEmailOrUsername(LoginRequestDTO request) {
        UserEntity user = null;
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user = userRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase())
                    .orElse(null);
        } else if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                    .orElse(null);
        }
        
        if (user == null) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
        
        return user;
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
        response.setUser(userInfo);
        
        return response;
    }
}