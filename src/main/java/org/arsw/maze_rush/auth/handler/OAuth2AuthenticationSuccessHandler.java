package org.arsw.maze_rush.auth.handler;

import java.io.IOException;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.service.OAuth2Service;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler que se ejecuta después de una autenticación OAuth2 exitosa.
 * Genera tokens JWT, los establece como cookies y redirige al frontend.
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oauth2Service;
    private final CookieUtil cookieUtil;
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
            OAuth2Service oauth2Service,
            CookieUtil cookieUtil,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl) {
        this.oauth2Service = oauth2Service;
        this.cookieUtil = cookieUtil;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            // Extraer información del usuario de Google
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String providerId = oAuth2User.getAttribute("sub");
            
            log.debug("OAuth2 authentication successful for email: {}", email);
            
            // Procesar el usuario y generar tokens JWT
            AuthResponseDTO authResponse = oauth2Service.processOAuth2User(email, name, providerId, null);
            
            // Establecer cookies con los tokens
            cookieUtil.setAuthCookies(
                response,
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getExpiresIn().intValue(),
                86400 // 24 horas para refresh token
            );
            
            // Redirigir al frontend con indicador de éxito
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("success", "true")
                    .build().toUriString();
            
            log.debug("Redirecting to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            
            // Redirigir al frontend con error
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("error", "authentication_failed")
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
