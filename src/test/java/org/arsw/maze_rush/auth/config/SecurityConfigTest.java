package org.arsw.maze_rush.auth.config;

import org.arsw.maze_rush.auth.filter.JwtAuthenticationFilter;
import org.arsw.maze_rush.auth.handler.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod; 
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock
    private CorsConfigurationSource corsConfigurationSource;
    @Mock
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @Mock
    private HttpSecurity httpSecurity;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, corsConfigurationSource, oAuth2AuthenticationSuccessHandler);
    }
    
    @Test
    void testPasswordEncoder() {
         assertTrue(securityConfig.passwordEncoder() instanceof BCryptPasswordEncoder);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"}) 
    void testSecurityFilterChain_WithOAuth2Handler() throws Exception {
        //  Configurar Mocks (Fluent API)
        when(httpSecurity.cors(any(Customizer.class))).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any(Customizer.class))).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any(Customizer.class))).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any(Customizer.class))).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any(Customizer.class))).thenReturn(httpSecurity);
        
        //  Ejecutar
        securityConfig.securityFilterChain(httpSecurity);

        // CAPTURAR Y EJECUTAR LAMBDAS

        // --- CORS ---
        ArgumentCaptor<Customizer<CorsConfigurer<HttpSecurity>>> corsCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(httpSecurity).cors(corsCaptor.capture());
        CorsConfigurer<HttpSecurity> corsConfigurer = mock(CorsConfigurer.class);
        corsCaptor.getValue().customize(corsConfigurer);
        verify(corsConfigurer).configurationSource(corsConfigurationSource);

        // --- CSRF ---
        ArgumentCaptor<Customizer<CsrfConfigurer<HttpSecurity>>> csrfCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(httpSecurity).csrf(csrfCaptor.capture());

        // --- SESSION ---
        ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> sessionCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(httpSecurity).sessionManagement(sessionCaptor.capture());
        SessionManagementConfigurer<HttpSecurity> sessionConfigurer = mock(SessionManagementConfigurer.class);
        sessionCaptor.getValue().customize(sessionConfigurer);
        verify(sessionConfigurer).sessionCreationPolicy(any());

        // --- AUTHORIZE REQUESTS ---
        ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> authCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(httpSecurity).authorizeHttpRequests(authCaptor.capture());
        
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry = 
                mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
        
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = 
                mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
        when(authorizedUrl.permitAll()).thenReturn(registry);
        when(authorizedUrl.authenticated()).thenReturn(registry);

        when(registry.requestMatchers(any(HttpMethod.class), any(String[].class))).thenReturn(authorizedUrl);
        when(registry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
        when(registry.anyRequest()).thenReturn(authorizedUrl);
        
        authCaptor.getValue().customize(registry);
        
        // Verificamos que se llamó con POST para login
        verify(registry, atLeastOnce()).requestMatchers(eq(HttpMethod.POST), any(String[].class)); 
        verify(registry).anyRequest();

        // --- E. OAUTH2 ---
        ArgumentCaptor<Customizer<OAuth2LoginConfigurer<HttpSecurity>>> oauthCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(httpSecurity).oauth2Login(oauthCaptor.capture());
        
        OAuth2LoginConfigurer<HttpSecurity> oauthConfigurer = mock(OAuth2LoginConfigurer.class);
        oauthCaptor.getValue().customize(oauthConfigurer);
        verify(oauthConfigurer).successHandler(oAuth2AuthenticationSuccessHandler);

        //  Verificar Filtro
        verify(httpSecurity).addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
    }
}