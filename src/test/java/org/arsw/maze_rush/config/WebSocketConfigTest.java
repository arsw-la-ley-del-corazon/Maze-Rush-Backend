package org.arsw.maze_rush.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    @Mock
    private SimpleBrokerRegistration simpleBrokerRegistration;

    @InjectMocks
    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webSocketConfig, "allowedOrigins", "http://localhost:3000,http://example.com");
    }

    // Test para verificar la configuración del Message Broker.
     
    @Test
    void testConfigureMessageBroker() {
        when(messageBrokerRegistry.enableSimpleBroker(any(String[].class))).thenReturn(simpleBrokerRegistration);

        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).enableSimpleBroker("/topic", "/queue");
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(messageBrokerRegistry).setUserDestinationPrefix("/user");
    }

    // Test para verificar el registro de Endpoints STOMP.

    @Test
    void testRegisterStompEndpoints() {
        //  Cuando se llame a addEndpoint("/ws"), devolver el mock de registro
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        
        // Cuando se llame a setAllowedOriginPatterns(...), devolver el mismo mock de registro (para poder seguir encadenando)
        when(endpointRegistration.setAllowedOriginPatterns(any(String[].class))).thenReturn(endpointRegistration);

        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Assert:Verificamos que se registró el endpoint correcto
        verify(stompEndpointRegistry).addEndpoint("/ws");
        
        // Verificamos que se pasaron los orígenes parseados correctamente (split por coma)
        verify(endpointRegistration).setAllowedOriginPatterns("http://localhost:3000", "http://example.com");
        
        // Verificamos que se habilitó SockJS
        verify(endpointRegistration).withSockJS();
    }
}