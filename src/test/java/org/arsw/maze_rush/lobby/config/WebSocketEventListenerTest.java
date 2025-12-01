package org.arsw.maze_rush.lobby.config;

import org.arsw.maze_rush.lobby.service.LobbyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketEventListenerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private WebSocketEventListener listener;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    //  Conexión WS → NO debe fallar

    @Test
    void testHandleWebSocketConnectListener() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("session123");

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionConnectedEvent event =
                new SessionConnectedEvent(this, message);

        assertDoesNotThrow(() -> listener.handleWebSocketConnectListener(event));
        
    }


    //  Desconexión válida → llama leaveLobby()
    @Test
    void testHandleWebSocketDisconnectListener_UserAndLobbyPresent() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("ABC123");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("username", "john");
        sessionAttrs.put("lobbyCode", "L1");

        accessor.setSessionAttributes(sessionAttrs);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event =
                new SessionDisconnectEvent(
                        this,
                        message,
                        "ABC123",
                        CloseStatus.NORMAL
                );

        listener.handleWebSocketDisconnectListener(event);

        verify(lobbyService).leaveLobby("L1", "john");
    }

    //  Sin atributos → NO llama leaveLobby()
    @Test
    void testHandleWebSocketDisconnectListener_NoSessionAttributes() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("XYZ");
        accessor.setSessionAttributes(null);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event =
                new SessionDisconnectEvent(
                        this,
                        message,
                        "XYZ",
                        CloseStatus.NORMAL
                );

        listener.handleWebSocketDisconnectListener(event);

        verify(lobbyService, never()).leaveLobby(any(), any());
    }

    //  Falta lobbyCode → NO llama leaveLobby()
    @Test
    void testHandleWebSocketDisconnectListener_IncompleteAttributes() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("123");

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("username", "john");
        accessor.setSessionAttributes(attrs);

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event =
                new SessionDisconnectEvent(
                        this,
                        message,
                        "123",
                        CloseStatus.NORMAL
                );

        listener.handleWebSocketDisconnectListener(event);

        verify(lobbyService, never()).leaveLobby(any(), any());
    }

    //  leaveLobby lanza excepción → NO debe romper flujo
    @Test
    void testHandleWebSocketDisconnectListener_LeaveLobbyThrowsException() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("777");

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("username", "john");
        attrs.put("lobbyCode", "L1");
        accessor.setSessionAttributes(attrs);

        doThrow(new RuntimeException("err"))
                .when(lobbyService)
                .leaveLobby("L1", "john");

        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent event =
                new SessionDisconnectEvent(
                        this,
                        message,
                        "777",
                        CloseStatus.NORMAL
                );

        listener.handleWebSocketDisconnectListener(event);

        verify(lobbyService).leaveLobby("L1", "john");
    }


    //  Suscripción válida → guarda atributos
    @Test
    void testHandleSubscribeEvent_ValidLobbySubscription() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/lobby/ABCD123/players");

        Map<String, Object> sessionAttrs = new HashMap<>();
        accessor.setSessionAttributes(sessionAttrs);

        Principal principal = () -> "john";
        accessor.setUser(principal);

        Message<byte[]> msg =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionSubscribeEvent event =
                new SessionSubscribeEvent(this, msg);

        listener.handleSubscribeEvent(event);

        assertEquals("john", sessionAttrs.get("username"));
        assertEquals("ABCD123", sessionAttrs.get("lobbyCode"));
    }

    //  Destino inválido → NO guarda nada
    @Test
    void testHandleSubscribeEvent_InvalidDestination() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/other/xxx");

        Map<String, Object> attrs = new HashMap<>();
        accessor.setSessionAttributes(attrs);

        Principal principal = () -> "john";
        accessor.setUser(principal);

        Message<byte[]> msg =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionSubscribeEvent event =
                new SessionSubscribeEvent(this, msg);

        listener.handleSubscribeEvent(event);

        assertTrue(attrs.isEmpty());
    }

    //  Test extractLobbyCodeFromDestination()
    @Test
    void testExtractLobbyCodeFromDestination() {
        assertEquals("ROOM99",
                listener.extractLobbyCodeFromDestination("/topic/lobby/ROOM99/players"));
    }

    @Test
    void testExtractLobbyCode_NullOrInvalid() {
        assertNull(listener.extractLobbyCodeFromDestination(null));
        assertNull(listener.extractLobbyCodeFromDestination("/topic/other/123"));
        assertNull(listener.extractLobbyCodeFromDestination("/topic/lobby"));
    }
}
