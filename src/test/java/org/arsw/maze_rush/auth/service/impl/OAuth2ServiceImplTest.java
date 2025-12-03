package org.arsw.maze_rush.auth.service.impl;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.OAuth2LoginRequestDTO;
import org.arsw.maze_rush.auth.service.OAuth2Service;
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
    private OAuth2Service selfMock;

    private OAuth2ServiceImpl oauth2Service;

    private final String googleClientID = "my-google-client-id";

    @BeforeEach
    void setUp() {
        oauth2Service = new OAuth2ServiceImpl(userRepository, jwtUtil, googleClientID);
        ReflectionTestUtils.setField(oauth2Service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(oauth2Service, "self", selfMock); 
    }


    // PRUEBAS DE authenticateWithGoogle

    @Test
    void testAuthenticateWithGoogle_Success() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("valid-token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", googleClientID);
        googleResponse.put("email", "test@gmail.com");
        googleResponse.put("name", "Test User");
        googleResponse.put("sub", "123456");
        googleResponse.put("email_verified", true);

        //  Mock RestTemplate (Validación Google OK)
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        // Mock de la llamada interna a self.processOAuth2User
        AuthResponseDTO expectedResponse = new AuthResponseDTO();
        expectedResponse.setAccessToken("mocked-access-token");
        
        when(selfMock.processOAuth2User(anyString(), anyString(), anyString(), any()))
                .thenReturn(expectedResponse);

        //  Ejecutar
        AuthResponseDTO response = oauth2Service.authenticateWithGoogle(request);

        //  Verificar
        assertNotNull(response);
        assertEquals("mocked-access-token", response.getAccessToken());
        
        // Verificamos que se llamó al método transaccional a través del proxy (self)
        verify(selfMock).processOAuth2User(eq("test@gmail.com"), eq("Test User"), eq("123456"), any());
    }

    @Test
    void testAuthenticateWithGoogle_NetworkError() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");

        when(restTemplate.exchange(
                anyString(), any(), any(), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenThrow(new RestClientException("Connection refused"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> oauth2Service.authenticateWithGoogle(request));
        assertTrue(ex.getMessage().contains("Error al validar token"));
    }

    @Test
    void testAuthenticateWithGoogle_InvalidStatusCode() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        
        when(restTemplate.exchange(
                anyString(), any(), any(), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> oauth2Service.authenticateWithGoogle(request));
        String expectedMessage = "Token de Google inválido";
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testAuthenticateWithGoogle_AudienceMismatch() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", "OTHER_CLIENT_ID");

        when(restTemplate.exchange(
                anyString(), any(), any(), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> oauth2Service.authenticateWithGoogle(request));
        assertTrue(ex.getMessage().contains("no válido para esta aplicación"));
    }

    @Test
    void testAuthenticateWithGoogle_EmailNotVerified_Boolean() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", googleClientID);
        googleResponse.put("email_verified", false);

        when(restTemplate.exchange(
                anyString(), any(), any(), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        assertThrows(UnauthorizedException.class, () -> oauth2Service.authenticateWithGoogle(request));
    }

    @Test
    void testAuthenticateWithGoogle_EmailNotVerified_String() {
        OAuth2LoginRequestDTO request = new OAuth2LoginRequestDTO("token");
        Map<String, Object> googleResponse = new HashMap<>();
        googleResponse.put("aud", googleClientID);
        googleResponse.put("email_verified", "false");

        when(restTemplate.exchange(
                anyString(), any(), any(), 
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(new ResponseEntity<>(googleResponse, HttpStatus.OK));

        assertThrows(UnauthorizedException.class, () -> oauth2Service.authenticateWithGoogle(request));
    }

    // PRUEBAS DE processOAuth2User
    @Test
    void testProcessOAuth2User_ExistingGoogleUser_NoUpdate() {
        String email = "test@gmail.com";
        String providerId = "123";
        UserEntity existing = new UserEntity();
        existing.setId(UUID.randomUUID());
        existing.setAuthProvider(AuthProvider.GOOGLE);
        existing.setProviderId("123");
        existing.setUsername("existingUser");

        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(email, AuthProvider.GOOGLE)).thenReturn(Optional.of(existing));
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

        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(email, AuthProvider.GOOGLE)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User(email, "Name", "new_id", null);
        verify(userRepository).save(existing);
        assertEquals("new_id", existing.getProviderId());
    }

    @Test
    void testProcessOAuth2User_LocalUserConflict() {
        String email = "conflict@gmail.com";
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(email, AuthProvider.GOOGLE)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(email, AuthProvider.LOCAL)).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UnauthorizedException.class, () -> oauth2Service.processOAuth2User(email, "Name", "123", null));
    }

    // PRUEBAS DE GENERACIÓN DE USERNAME

    private void mockRepositorySaveWithId() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
    }

    @Test
    void testUsernameGeneration_SimpleName() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
        mockRepositorySaveWithId();
        
        when(userRepository.existsByUsernameIgnoreCase("juanperez")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("j.perez@mail.com", "Juan Perez", "1", null);
        verify(jwtUtil).generateAccessToken("juanperez");
    }

    @Test
    void testUsernameGeneration_NameTaken_UseSuffix() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase("juanperez")).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase("juanperez1")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("mail@m.com", "Juan Perez", "1", null);
        verify(jwtUtil).generateAccessToken("juanperez1");
    }

    @Test
    void testUsernameGeneration_NameEmpty_UseEmail() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase("miemail")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("miemail@test.com", "", "1", null);
        verify(jwtUtil).generateAccessToken("miemail");
    }

    @Test
    void testUsernameGeneration_NameLoopExhausted() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase(startsWith("juan"))).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase("otro")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("otro@test.com", "Juan", "1", null);
        verify(jwtUtil).generateAccessToken("otro");
    }
    
    @Test
    void testUsernameGeneration_EmailLoop() {
        when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
        mockRepositorySaveWithId();

        when(userRepository.existsByUsernameIgnoreCase("test")).thenReturn(true); 
        when(userRepository.existsByUsernameIgnoreCase("test1")).thenReturn(true); 
        when(userRepository.existsByUsernameIgnoreCase("test2")).thenReturn(false);
        when(jwtUtil.generateAccessToken(anyString())).thenReturn("token");

        oauth2Service.processOAuth2User("test@mail.com", "", "1", null);
        verify(jwtUtil).generateAccessToken("test2");
    }

    @Test
    void testUsernameGeneration_FinalFallback() {
         when(userRepository.findByEmailIgnoreCaseAndAuthProvider(any(), any())).thenReturn(Optional.empty());
         mockRepositorySaveWithId();

         when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);
         
         when(jwtUtil.generateAccessToken(anyString())).thenReturn("access-token");
         when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

         oauth2Service.processOAuth2User("x@x.com", "", "1", null);
         
         ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
         verify(jwtUtil, atLeastOnce()).generateAccessToken(usernameCaptor.capture());
         
         String generatedUsername = usernameCaptor.getValue();
         assertNotNull(generatedUsername);
         assertTrue(generatedUsername.startsWith("x"));
         assertTrue(generatedUsername.length() > 5);
    }
}