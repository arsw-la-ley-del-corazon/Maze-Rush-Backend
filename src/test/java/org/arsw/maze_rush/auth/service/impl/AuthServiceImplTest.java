package org.arsw.maze_rush.auth.service.impl;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("testUser");
        user.setEmail("test@mail.com");
        user.setScore(10);
        user.setLevel(2);
    }

    // REFRESH TOKEN OK
    @Test
    void testRefreshToken_ok() {

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("validRefresh");

        when(jwtUtil.validateToken("validRefresh")).thenReturn(true);
        when(jwtUtil.getTokenType("validRefresh")).thenReturn("refresh");
        when(jwtUtil.getUsernameFromToken("validRefresh")).thenReturn("testUser");
        when(userRepository.findByUsernameIgnoreCase("testUser"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateAccessToken("testUser"))
                .thenReturn("newAccess");
        when(jwtUtil.generateRefreshToken("testUser"))
                .thenReturn("newRefresh");
        when(jwtUtil.getAccessTokenExpiration())
                .thenReturn(3600L);

        AuthResponseDTO result = authService.refreshToken(dto);

        assertNotNull(result);
        assertEquals("newAccess", result.getAccessToken());
        assertEquals("newRefresh", result.getRefreshToken());
        assertEquals(3600L, result.getExpiresIn());
    }

    // REFRESH TOKEN INVALIDO
    @Test
    void testRefreshToken_invalidToken() {

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("bad");

        when(jwtUtil.validateToken("bad")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(dto));
    }

    // REFRESH TOKEN TIPO INCORRECTO
    @Test
    void testRefreshToken_wrongType() {

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("token123");

        when(jwtUtil.validateToken("token123")).thenReturn(true);
        when(jwtUtil.getTokenType("token123")).thenReturn("access");

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(dto));
    }

    // REFRESH TOKEN USUARIO NO EXISTE
    @Test
    void testRefreshToken_userNotFound() {

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken("ref123");

        when(jwtUtil.validateToken("ref123")).thenReturn(true);
        when(jwtUtil.getTokenType("ref123")).thenReturn("refresh");
        when(jwtUtil.getUsernameFromToken("ref123")).thenReturn("ghostUser");
        when(userRepository.findByUsernameIgnoreCase("ghostUser"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(dto));
    }

    // LOGOUT
    @Test
    void testLogout_validToken() {

        when(jwtUtil.validateToken("acc")).thenReturn(true);

        authService.logout("acc");

        assertFalse(authService.validateToken("acc"));
    }

    @Test
    void testLogout_invalidToken() {

        when(jwtUtil.validateToken("bad")).thenReturn(false);

        authService.logout("bad");

        assertFalse(authService.validateToken("bad"));
    }

    // VALIDATE TOKEN
    @Test
    void testValidateToken_ok() {

        when(jwtUtil.validateToken("abc")).thenReturn(true);

        assertTrue(authService.validateToken("abc"));
    }

    @Test
    void testValidateToken_blacklisted() {

        when(jwtUtil.validateToken("abc")).thenReturn(true);
        authService.logout("abc");

        assertFalse(authService.validateToken("abc"));
    }
}
