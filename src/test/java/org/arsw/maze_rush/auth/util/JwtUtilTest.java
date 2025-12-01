package org.arsw.maze_rush.auth.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String SECRET_KEY_STRING = 
        "EstaEsUnaClaveSuperSecretaYLoSuficientementeLargaParaCumplirConElRequisitoDe64BytesDelAlgoritmoHS512DeJWT_1234567890";
    
    private static final long ACCESS_EXP = 3600L;
    private static final long REFRESH_EXP = 86400L;

    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY_STRING, ACCESS_EXP, REFRESH_EXP);
    }

    /**
     * Test para el constructor: Caso donde NO se pasa secreto.
     * Debe generar una clave aleatoria interna.
     */
    @Test
    void testConstructor_NoSecret_ShouldGenerateKey() {
        JwtUtil autoKeyUtil = new JwtUtil(null, ACCESS_EXP, REFRESH_EXP);
        String token = autoKeyUtil.generateAccessToken("user");
        
        assertNotNull(token);
        assertTrue(autoKeyUtil.validateToken(token));
        
        JwtUtil emptySecretUtil = new JwtUtil("", ACCESS_EXP, REFRESH_EXP);
        assertNotNull(emptySecretUtil.generateAccessToken("user"));
    }

    /**
     * Test para el constructor: Caso donde el secreto es muy corto.
     * Debe lanzar IllegalArgumentException.
     */
    @Test
    void testConstructor_ShortSecret_ShouldThrowException() {
        String shortSecret = "too_short";
        assertThrows(IllegalArgumentException.class, () -> {
            new JwtUtil(shortSecret, ACCESS_EXP, REFRESH_EXP);
        });
    }

    
    // Test: Generar token desde Authentication.
     
    @Test
    void testGenerateAccessToken_FromAuthentication() {
        when(authentication.getName()).thenReturn("testUser");
        
        String token = jwtUtil.generateAccessToken(authentication);
        
        assertNotNull(token);
        assertEquals("testUser", jwtUtil.getUsernameFromToken(token));
        assertEquals("access", jwtUtil.getTokenType(token));
    }

    
    // Test: Generar Refresh Token.

    @Test
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken("testUser");
        
        assertNotNull(token);
        assertEquals("testUser", jwtUtil.getUsernameFromToken(token));
        assertEquals("refresh", jwtUtil.getTokenType(token));
    }

    
    // Test: Validar token correcto.
    
    @Test
    void testValidateToken_Valid() {
        String token = jwtUtil.generateAccessToken("user");
        assertTrue(jwtUtil.validateToken(token));
    }

    /**
     * Test: Validar token incorrecto (Malformado o firma inválida).
     * Cubre el catch (JwtException | IllegalArgumentException).
     */
    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.structure"));
        assertFalse(jwtUtil.validateToken(null));
        
        JwtUtil otherUtil = new JwtUtil(null, ACCESS_EXP, REFRESH_EXP);
        String otherToken = otherUtil.generateAccessToken("user");
        assertFalse(jwtUtil.validateToken(otherToken));
    }

    /**
     * Test: Verificar expiración de token.
     * Estrategia: Crear un JwtUtil con tiempo negativo para simular expiración inmediata.
     */
    @Test
    void testTokenExpiration() {
        //  Token válido
        String validToken = jwtUtil.generateAccessToken("user");
        assertFalse(jwtUtil.isTokenExpired(validToken));
        
        //  Token expirado (Instanciamos JwtUtil con expiración negativa: -10 segundos)
        JwtUtil expiredUtil = new JwtUtil(SECRET_KEY_STRING, -10, -10);
        String expiredToken = expiredUtil.generateAccessToken("user");
        
        assertTrue(jwtUtil.isTokenExpired(expiredToken));
    }
    
    /**
     * Test: isTokenExpired con token malformado.
     * Cubre el catch(JwtException) dentro de isTokenExpired.
     */
    @Test
    void testIsTokenExpired_Malformed() {
        assertTrue(jwtUtil.isTokenExpired("malformed_token"));
    }

    // Test: Obtener fecha de expiración.
    @Test
    void testGetExpirationDateFromToken() {
        String token = jwtUtil.generateAccessToken("user");
        Date expiration = jwtUtil.getExpirationDateFromToken(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    // Test: Getters simples de configuración.

    @Test
    void testConfigGetters() {
        assertEquals(ACCESS_EXP, jwtUtil.getAccessTokenExpiration());
        assertEquals(REFRESH_EXP, jwtUtil.getRefreshTokenExpiration());
    }
    
    /**
     * Test: Verificar que se lanza excepción si intentamos parsear un token malo
     * en métodos que no tienen try-catch (como getUsernameFromToken).
     */
    @Test
    void testGetUsernameFromToken_Invalid() {
        assertThrows(JwtException.class, () -> {
             jwtUtil.getUsernameFromToken("bad.token");
        });
    }
}