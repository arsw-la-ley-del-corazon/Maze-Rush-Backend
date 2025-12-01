package org.arsw.maze_rush.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utilidad para manejo de cookies JWT de forma segura
 */
@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    
    private final boolean isProduction;
    private final String domain;
    
    public CookieUtil(
            @Value("${spring.profiles.active:dev}") String activeProfile,
            @Value("${app.cookie.domain:}") String domain) {
        this.isProduction = "prod".equals(activeProfile) || "production".equals(activeProfile);
        this.domain = domain != null && !domain.isBlank() ? domain : null;
    }

    /**
     * Crea una cookie de acceso JWT
     */
    public Cookie createAccessTokenCookie(String token, int maxAgeSeconds) {
        return createCookie(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds, true);
    }

    /**
     * Crea una cookie de refresh token JWT
     */
    public Cookie createRefreshTokenCookie(String token, int maxAgeSeconds) {
        return createCookie(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds, true);
    }

    /**
     * Crea una cookie con las configuraciones de seguridad apropiadas
     */
    private Cookie createCookie(String name, String value, int maxAgeSeconds, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAgeSeconds);
        
        // En producción usar Secure y SameSite=None para permitir CORS
        // En desarrollo no usar Secure para permitir HTTP
        if (isProduction) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        
        // Establecer dominio si está configurado
        if (domain != null) {
            cookie.setDomain(domain);
        }
        
        return cookie;
    }

    /**
     * Obtiene el valor de una cookie por nombre
     */
    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Elimina una cookie estableciendo su tiempo de vida a 0
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        
        if (domain != null) {
            cookie.setDomain(domain);
        }
        
        response.addCookie(cookie);
    }

    /**
     * Elimina todas las cookies de autenticación
     */
    public void deleteAuthCookies(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    /**
     * Establece las cookies de autenticación en la respuesta
     */
    public void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken, 
                              int accessTokenMaxAge, int refreshTokenMaxAge) {
        response.addCookie(createAccessTokenCookie(accessToken, accessTokenMaxAge));
        response.addCookie(createRefreshTokenCookie(refreshToken, refreshTokenMaxAge));
    }
}
