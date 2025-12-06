package org.arsw.maze_rush.auth.service.impl;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.entities.AuthProvider;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuth2ServiceImpl selfMock;

    private OAuth2ServiceImpl oauth2Service;

    private final String googleClientID = "my-google-client-id";

    @BeforeEach
    void setUp() {
        oauth2Service = new OAuth2ServiceImpl(userRepository, jwtUtil, googleClientID, selfMock);
        ReflectionTestUtils.setField(oauth2Service, "restTemplate", restTemplate);
    }

    // --- PRUEBAS DE authenticateWithGoogle ---
    @Test
    void testAuthenticateWithGoogle_Success() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("valid-token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", googleClientID);
        googleResponse.put("email", "test@gmail.com");
        googleResponse.put("name", "Test User");
        googleResponse.put("sub", "123456");
        googleResponse.put("email_verified", true);

        // Mock de Google API
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        // Crear el objeto DTO usando el Builder correcto
        AuthResponseDTO.UserInfo userInfo = AuthResponseDTO.UserInfo.builder()
                .id(UUID.randomUUID().toString())
                .username("test")
                .email("test@gmail.com")
                .score(0)
                .level(1)
                .build();

        AuthResponseDTO dummyResponse = AuthResponseDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .expiresIn(3600L)
                .user(userInfo)
                .build();
        
        // Mockear self.processOAuth2User
        when(selfMock.processOAuth2User(
                eq("test@gmail.com"), 
                eq("Test User"), 
                eq("123456"), 
                isNull()
        )).thenReturn(dummyResponse);

        // Ejecutar
        AuthResponseDTO response = oauth2Service.authenticateWithGoogle(request);

        // Verificar
        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        assertEquals("test@gmail.com", response.getUser().getEmail()); 
        
        verify(selfMock).processOAuth2User(
                eq("test@gmail.com"), 
                eq("Test User"), 
                eq("123456"), 
                isNull()
        );
    }

    @Test
    void testAuthenticateWithGoogle_NetworkError() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenThrow(new RestClientException("Connection refused"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, 
            () -> oauth2Service.authenticateWithGoogle(request));
        assertTrue(ex.getMessage().contains("Error al validar token"));
    }

    @Test
    void testAuthenticateWithGoogle_InvalidStatusCode() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET),
                any(HttpEntity.class), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, 
            () -> oauth2Service.authenticateWithGoogle(request));
        assertEquals("Token de Google inválido", ex.getMessage());
    }

    @Test
    void testAuthenticateWithGoogle_AudienceMismatch() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", "OTHER_CLIENT_ID");

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET),
                any(HttpEntity.class), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, 
            () -> oauth2Service.authenticateWithGoogle(request));
        assertTrue(ex.getMessage().contains("no válido para esta aplicación"));
    }

    @Test
    void testAuthenticateWithGoogle_EmailNotVerified() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", googleClientID);
        googleResponse.put("email_verified", false);

        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET),
                any(HttpEntity.class), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        assertThrows(UnauthorizedException.class, 
            () -> oauth2Service.authenticateWithGoogle(request));
    }

    // --- PRUEBAS DE processOAuth2User ---

    @Test
    void testProcessOAuth2User_ExistingGoogleUser_NoUpdate() {
        String email = "test@gmail.com";
        String providerId = "123";
        UserEntity existing = new UserEntity();
        existing.setId(UUID.randomUUID());
        existing.setAuthProvider(AuthProvider.GOOGLE);
        existing.setProviderId("123");
        existing.setUsername("existingUser");

        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(
                email, 
                AuthProvider.GOOGLE)
        ).thenReturn(Optional.of(existing));
        
        when(jwtUtil.generateAccessToken("existingUser")).thenReturn("token");

        oauth2Service.processOAuth2User(email, "Name", providerId, null);
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void testProcessOAuth2User_ExistingGoogleUser_WithUpdate() {
        String email = "test@gmail.com";
        UserEntity existing = new UserEntity();
        existing.setId(UUID.randomUUID());
        existing.setAuthProvider(AuthProvider.GOOGLE);
        existing.setProviderId("old_id");
        existing.setUsername("user");

        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(
                email, 
                AuthProvider.GOOGLE)
        ).thenReturn(Optional.of(existing));
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(existing);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User(email, "Name", "new_id", null);
        
        verify(userRepository).save(any(UserEntity.class));
        assertEquals("new_id", existing.getProviderId());
    }

    @Test
    void testProcessOAuth2User_LocalUserConflict() {
        String email = "conflict@gmail.com";
        
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(
                email, 
                AuthProvider.GOOGLE)
        ).thenReturn(Optional.empty());
        
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(
                email, 
                AuthProvider.LOCAL)
        ).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UnauthorizedException.class, 
            () -> oauth2Service.processOAuth2User(email, "Name", "123", null));
    }

    // --- PRUEBAS DE GENERACIÓN DE USERNAME ---

    private void mockRepositorySaveWithId() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
    }

    @Test
    void testUsernameGeneration_SimpleName() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(anyString(), any(AuthProvider.class)))
                .thenReturn(Optional.empty());
        mockRepositorySaveWithId();
        
        when(userRepository.existsByUsernameIgnoreCase("juanperez")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("j.perez@mail.com", "Juan Perez", "1", null);
        
        verify(jwtUtil).generateAccessToken("juanperez");
    }

    @Test
    void testUsernameGeneration_NameTaken_UseSuffix() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(anyString(), any(AuthProvider.class)))
                .thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase("juanperez")).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase("juanperez1")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("mail@m.com", "Juan Perez", "1", null);
        
        verify(jwtUtil).generateAccessToken("juanperez1");
    }

    @Test
    void testUsernameGeneration_FinalFallback() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(anyString(), any(AuthProvider.class)))
                .thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);
        
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

        oauth2Service.processOAuth2User("x@x.com", "", "1", null);
        
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtUtil, atLeastOnce()).generateAccessToken(usernameCaptor.capture());
        
        String generatedUsername = usernameCaptor.getValue();
        assertNotNull(generatedUsername);
        assertTrue(generatedUsername.length() > 5); 
    }
}