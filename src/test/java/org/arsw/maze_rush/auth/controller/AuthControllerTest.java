package org.arsw.maze_rush.auth.controller;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private OAuth2Service oauth2Service;
    @Mock private CookieUtil cookieUtil;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthController controller;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null)
        );
    }

    @Test
    void testAuthenticateWithGoogle_ok() {
        OAuth2LoginRequestDTO login = new OAuth2LoginRequestDTO();
        login.setIdToken("valid-google-id");

        AuthResponseDTO auth = AuthResponseDTO.builder()
                .accessToken("acc")
                .refreshToken("ref")
                .expiresIn(3600L)
                .build();

        when(oauth2Service.authenticateWithGoogle(login)).thenReturn(auth);

        ResponseEntity<AuthResponseDTO> result =
                controller.authenticateWithGoogle(login, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("acc", result.getBody().getAccessToken());

        verify(cookieUtil).setAuthCookies(response, "acc", "ref", 3600, 86400);
    }

    @Test
    void testRefreshToken_ok() {
        request.addHeader("Authorization", "Bearer myRefresh");

        AuthResponseDTO auth = AuthResponseDTO.builder()
                .accessToken("newAcc")
                .refreshToken("newRef")
                .expiresIn(4800L)
                .build();

        when(authService.refreshToken(any())).thenReturn(auth);

        ResponseEntity<AuthResponseDTO> result =
                controller.refreshToken(request, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("newAcc", result.getBody().getAccessToken());
    }

    @Test
    void testLogout_ok() {
        request.addHeader("Authorization", "Bearer abc123");

        ResponseEntity<Void> result = controller.logout(request, response);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(authService).logout("abc123");
        verify(cookieUtil).deleteAuthCookies(response);
    }

    @Test
    void testLogout_noToken_ok() {
        ResponseEntity<Void> result = controller.logout(request, response);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(authService, never()).logout(any());
        verify(cookieUtil).deleteAuthCookies(response);
    }

    @Test
    void testValidateToken_valid() {
        request.addHeader("Authorization", "Bearer accToken");
        when(authService.validateToken("accToken")).thenReturn(true);

        ResponseEntity<Boolean> result = controller.validateToken(request);
        assertTrue(result.getBody());
    }

    @Test
    void testValidateToken_noToken() {
        ResponseEntity<Boolean> result = controller.validateToken(request);
        assertFalse(result.getBody());
    }

    @Test
    void testGetCurrentUser_ok() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("testUser");
        user.setEmail("test@mail.com");
        user.setScore(10);
        user.setLevel(2);

        when(userRepository.findByUsernameIgnoreCase("testUser"))
                .thenReturn(Optional.of(user));

        ResponseEntity<AuthResponseDTO.UserInfo> result =
                controller.getCurrentUser();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("testUser", result.getBody().getUsername());
    }

    @Test
    void testGetCurrentUser_notFound() {
        when(userRepository.findByUsernameIgnoreCase("testUser"))
                .thenReturn(Optional.empty());

        ResponseEntity<AuthResponseDTO.UserInfo> result =
                controller.getCurrentUser();

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void testExtractToken_fromCookie() throws Exception {
        when(cookieUtil.getCookieValue(any(), eq("refresh_token")))
                .thenReturn(Optional.of("cookieTok"));

        Method m = controller.getClass()
                .getDeclaredMethod("extractToken", HttpServletRequest.class, String.class);
        m.setAccessible(true);

        String token = (String) m.invoke(controller, request, "refresh_token");

        assertEquals("cookieTok", token);
    }

    @Test
    void testExtractToken_fromHeader() throws Exception {
        when(cookieUtil.getCookieValue(any(), anyString()))
                .thenReturn(Optional.empty());

        request.addHeader("Authorization", "Bearer headTok");

        Method m = controller.getClass()
                .getDeclaredMethod("extractToken", HttpServletRequest.class, String.class);
        m.setAccessible(true);

        String token = (String) m.invoke(controller, request, "access_token");

        assertEquals("headTok", token);
    }

    @Test
    void testExtractToken_none() throws Exception {
        when(cookieUtil.getCookieValue(any(), anyString()))
                .thenReturn(Optional.empty());

        Method m = controller.getClass()
                .getDeclaredMethod("extractToken", HttpServletRequest.class, String.class);
        m.setAccessible(true);

        String token = (String) m.invoke(controller, request, "refresh_token");

        assertNull(token);
    }
}
