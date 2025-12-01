package org.arsw.maze_rush.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

    @InjectMocks
    private SecurityHeadersFilter filter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private static final String COOP_HEADER = "Cross-Origin-Opener-Policy";
    private static final String COEP_HEADER = "Cross-Origin-Embedder-Policy";
    private static final String CONTENT_TYPE_HEADER = "X-Content-Type-Options";
    private static final String FRAME_OPTIONS_HEADER = "X-Frame-Options";
    private static final String XSS_PROTECTION_HEADER = "X-XSS-Protection";

    @Test
    void testDoFilterInternal_ShouldSetAllSecurityHeaders() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Verificar todos los encabezados COOP y COEP
        verify(response, times(1)).setHeader(COOP_HEADER, "same-origin-allow-popups");
        verify(response, times(1)).setHeader(COEP_HEADER, "unsafe-none");

        // Assert: Verificar encabezados de seguridad adicionales
        verify(response, times(1)).setHeader(CONTENT_TYPE_HEADER, "nosniff");
        verify(response, times(1)).setHeader(FRAME_OPTIONS_HEADER, "SAMEORIGIN");
        verify(response, times(1)).setHeader(XSS_PROTECTION_HEADER, "1; mode=block");

        // Assert: Verificar que el filtro llama al siguiente en la cadena
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilterOrder_IsHighestPrecedence() {
        // Assert: Verifica la anotación @Order(Ordered.HIGHEST_PRECEDENCE)
        assertEquals(org.springframework.core.Ordered.HIGHEST_PRECEDENCE, filter.getClass().getAnnotation(org.springframework.core.annotation.Order.class).value(), "El filtro debe tener la prioridad más alta.");
    }
}