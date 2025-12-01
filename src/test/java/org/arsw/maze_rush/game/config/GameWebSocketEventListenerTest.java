package org.arsw.maze_rush.game.config;

import org.arsw.maze_rush.game.dto.GameEventDTO;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameWebSocketEventListenerTest {

    private GameSessionManager sessionManager;
    private SimpMessagingTemplate messagingTemplate;
    private GameWebSocketEventListener eventListener;

    private final String username = "playerTest";
    private final String lobbyCode = "ABCD";
    private final String sessionId = "12345";

    @BeforeEach
    void setUp() {
        sessionManager = mock(GameSessionManager.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        eventListener = new GameWebSocketEventListener(sessionManager, messagingTemplate);
        when(sessionManager.sessionExists(lobbyCode)).thenReturn(true);
    }

    // Método de utilidad para crear el evento de desconexión
    private SessionDisconnectEvent createDisconnectEvent(String username, String lobbyCode) {
        //  Crear el objeto StompHeaderAccessor
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        
        //  Crear y configurar los atributos de sesión
        Map<String, Object> sessionAttributes = new HashMap<>();
        if (username != null) {
            sessionAttributes.put("username", username);
        }
        if (lobbyCode != null) {
            sessionAttributes.put("lobbyCode", lobbyCode);
        }
        headerAccessor.setSessionAttributes(sessionAttributes);
        
        //  Crear el mensaje
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
                .setHeaders(headerAccessor)
                .build();
        
        //  Crear el evento de desconexión
        return new SessionDisconnectEvent(this, message, sessionId, null);
    }

    // Tests de Lógica de Desconexión

    @Test
    void handleWebSocketDisconnect_SuccessfulRemovalAndNotification() {
        SessionDisconnectEvent event = createDisconnectEvent(username, lobbyCode);

        eventListener.handleWebSocketDisconnect(event);

        // Verificar que se llamó a remover el jugador
        verify(sessionManager).removePlayer(lobbyCode, username);
        
        // Verificar que se envió la notificación
        ArgumentCaptor<GameEventDTO> eventCaptor = ArgumentCaptor.forClass(GameEventDTO.class);
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/" + lobbyCode + "/move"),
            eventCaptor.capture()
        );

        // Verificar el contenido del DTO enviado
        GameEventDTO sentEvent = eventCaptor.getValue();
        assertEquals("player_left", sentEvent.getType());
        assertEquals(username, sentEvent.getUsername());
    }

    @Test
    void handleWebSocketDisconnect_NoActionIfUsernameMissing() {
        SessionDisconnectEvent event = createDisconnectEvent(null, lobbyCode);

        eventListener.handleWebSocketDisconnect(event);

        // Assert: No debe intentar remover ni notificar
        verify(sessionManager, never()).removePlayer(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
    
    @Test
    void handleWebSocketDisconnect_NoActionIfLobbyCodeMissing() {
        // Arrange: Falta el lobbyCode en los atributos de sesión
        SessionDisconnectEvent event = createDisconnectEvent(username, null);
        
        eventListener.handleWebSocketDisconnect(event);

        // Assert:No debe intentar remover ni notificar
        verify(sessionManager, never()).removePlayer(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
    
    @Test
    void handleWebSocketDisconnect_NoActionIfSessionDoesNotExist() {
        // Arrange: La sesión no existe
        when(sessionManager.sessionExists(lobbyCode)).thenReturn(false);
        SessionDisconnectEvent event = createDisconnectEvent(username, lobbyCode);
        
        eventListener.handleWebSocketDisconnect(event);

        // Assert: No debe intentar remover ni notificar si la sesión no está activa
        verify(sessionManager, never()).removePlayer(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}