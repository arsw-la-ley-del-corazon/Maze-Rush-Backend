package org.arsw.maze_rush.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthService authService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CookieUtil cookieUtil;
    
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Test : No hay token en Cookies ni en Header.
     */
    @Test
    void testDoFilterInternal_NoToken() throws ServletException, IOException {
        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.empty());
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test: Token presente en Cookie.
     */
    @Test
    void testDoFilterInternal_TokenInCookie_Success() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";

        //  Mock Cookie encontrada
        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.of(token));
        
        //  Mocks de validación
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(authService.validateToken(token)).thenReturn(true);
        
        //  Mock de repositorio (Usuario encontrado)
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(userEntity));

        // Ejecutar
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test: Token en Header (Fallback cuando no hay cookie).
     */
    @Test
    void testDoFilterInternal_TokenInHeader_Success() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "headerUser";
        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.empty());
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(authService.validateToken(token)).thenReturn(true);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(new UserEntity()));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Test: Header presente pero formato incorrecto .
     */
    @Test
    void testDoFilterInternal_InvalidHeaderFormat() throws ServletException, IOException {
        when(cookieUtil.getCookieValue(any(), anyString())).thenReturn(Optional.empty());
        when(request.getHeader("Authorization")).thenReturn("Basic 12345"); 

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test: Excepción al extraer username.
     */
    @Test
    void testDoFilterInternal_JwtException() throws ServletException, IOException {
        String token = "bad.token";
        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.of(token));
        when(jwtUtil.getUsernameFromToken(token)).thenThrow(new RuntimeException("Token invalido"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test : Token válido formato, pero AuthService dice que es inválido (expirado, firma mal, etc).
     */
    @Test
    void testDoFilterInternal_TokenInvalidByService() throws ServletException, IOException {
        String token = "expired.token";
        String username = "user";

        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.of(token));
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(authService.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Test : Usuario NO encontrado en base de datos.
     */
    @Test
    void testDoFilterInternal_UserNotFoundInDb() throws ServletException, IOException {
        String token = "valid.token";
        String username = "ghostUser";

        when(cookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE)).thenReturn(Optional.of(token));
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(authService.validateToken(token)).thenReturn(true);
        
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        UserDetails details = (UserDetails) auth.getPrincipal();
        assertFalse(details.isEnabled(), "El usuario debería estar deshabilitado (disabled=true) si no se encuentra en BD");
    }
}