package org.arsw.maze_rush.auth.handler;

import jakarta.servlet.ServletException;
import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private OAuth2Service oauth2Service;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    private OAuth2AuthenticationSuccessHandler successHandler;
    
    private final String frontendUrl = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(oauth2Service, cookieUtil, frontendUrl);
    }

    /**
     * Test : Flujo exitoso.
     */
    @Test
    void testOnAuthenticationSuccess_Success() throws IOException, ServletException {
        //  Preparar mocks de request/response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        //  Preparar datos del usuario OAuth2
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("sub")).thenReturn("12345");

        //  Preparar respuesta del servicio
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .expiresIn(3600L)
                .build();
        
        when(oauth2Service.processOAuth2User(anyString(), anyString(), anyString(), any()))
                .thenReturn(authResponse);

        //  Ejecutar el handler
        successHandler.onAuthenticationSuccess(request, response, authentication);

        //  Verificaciones
        // Verificar que se llamó al servicio con los datos correctos
        verify(oauth2Service).processOAuth2User("test@example.com", "Test User", "12345", null);
        // Verificar que se intentaron poner las cookies
        verify(cookieUtil).setAuthCookies(
                response,              
                "access-token-123",      
                "refresh-token-123",     
                3600,                  
                86400
        );
        // Verificar la redirección
        String redirectedUrl = response.getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertEquals("http://localhost:3000/oauth2/redirect?success=true", redirectedUrl);
    }

    /**
     * Test: Respuesta ya comprometida (Committed).
     */
    @Test
    void testOnAuthenticationSuccess_ResponseCommitted() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        response.setCommitted(true);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verifyNoInteractions(oauth2Service);
        verifyNoInteractions(cookieUtil);
        assertNull(response.getRedirectedUrl());
    }

    /**
     * Test : Excepción durante el proceso.
     */
    @Test
    void testOnAuthenticationSuccess_ExceptionThrown() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authentication.getPrincipal()).thenThrow(new RuntimeException("Error interno simulado"));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        String redirectedUrl = response.getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.startsWith("http://localhost:3000/oauth2/redirect"));
        assertTrue(redirectedUrl.contains("error=authentication_failed"));
    }
}