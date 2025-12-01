package org.arsw.maze_rush.lobby.controller;

import org.arsw.maze_rush.lobby.dto.ChatMessageDTO;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyWebSocketControllerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private LobbyWebSocketController controller;

    private Principal mockPrincipal;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("tester");
    }

    // TEST sendMessage()
    @Test
    void testSendMessage_ok() {

        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Hola!");
        payload.put("username", "tester");

        ChatMessageDTO result = controller.sendMessage(
                "ABC123",
                payload,
                mockPrincipal
        );

        assertEquals("tester", result.getUsername());
        assertEquals("Hola!", result.getMessage());

        verify(lobbyService).sendChatMessage("ABC123", "tester", "Hola!");
    }

    @Test
    void testSendMessage_missingUsername_usesPrincipal() {

        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Hi!");

        ChatMessageDTO result = controller.sendMessage(
                "ABC123",
                payload,
                mockPrincipal
        );

        assertEquals("tester", result.getUsername());
        assertEquals("Hi!", result.getMessage());

        verify(lobbyService).sendChatMessage("ABC123", "tester", "Hi!");
    }

    @Test
    void testSendMessage_noUsername_throws() {
        Map<String, String> payload = new HashMap<>();

        assertThrows(IllegalArgumentException.class,
                () -> controller.sendMessage("ABC", payload, null));
    }

    // TEST toggleReady()
    @Test
    void testToggleReady_ok() {

        Map<String, String> payload = Map.of("username", "tester");

        when(lobbyService.toggleReady("CODE", "tester"))
                .thenReturn(true);

        Map<String, Object> result = controller.toggleReady("CODE", payload, mockPrincipal);

        assertEquals("tester", result.get("username"));
        assertEquals(true, result.get("isReady"));

        verify(lobbyService).toggleReady("CODE", "tester");
    }

    @Test
    void testToggleReady_missingUsername_usesPrincipal() {

        Map<String, String> payload = new HashMap<>();

        when(lobbyService.toggleReady("LOBBY", "tester"))
                .thenReturn(false);

        Map<String, Object> result = controller.toggleReady("LOBBY", payload, mockPrincipal);

        assertEquals("tester", result.get("username"));
        assertEquals(false, result.get("isReady"));
    }

    // TEST startGame()
    @Test
    void testStartGame_ok() {

        Map<String, String> payload = Map.of("username", "tester");

        controller.startGame("XYZ", payload, mockPrincipal);

        verify(lobbyService).startGame("XYZ", "tester");
    }

    @Test
    void testStartGame_missingUsername_usesPrincipal() {

        Map<String, String> payload = new HashMap<>();

        controller.startGame("XYZ", payload, mockPrincipal);

        verify(lobbyService).startGame("XYZ", "tester");
    }

    @Test
    void testStartGame_missingAll_throws() {

        Map<String, String> payload = new HashMap<>();

        assertThrows(IllegalArgumentException.class,
                () -> controller.startGame("XYZ", payload, null));
    }

    // TEST handleConnect()
    @Test
    void testHandleConnect_ok() {

        Map<String, String> payload = Map.of("username", "tester");

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        controller.handleConnect(
                "LOBBY1",
                payload,
                mockPrincipal,
                accessor
        );

        assertEquals("tester", accessor.getSessionAttributes().get("username"));
        assertEquals("LOBBY1", accessor.getSessionAttributes().get("lobbyCode"));

        verify(lobbyService).notifyPlayersUpdate("LOBBY1");
    }

    @Test
    void testHandleConnect_missingUsername_usesPrincipal() {

        Map<String, String> payload = new HashMap<>();

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        controller.handleConnect("LOBBY2", payload, mockPrincipal, accessor);

        assertEquals("tester", accessor.getSessionAttributes().get("username"));
        verify(lobbyService).notifyPlayersUpdate("LOBBY2");
    }

    @Test
    void testHandleConnect_missingAll_noException() {
        Map<String, String> payload = new HashMap<>();

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        controller.handleConnect("CODE", payload, null, accessor);

        verify(lobbyService, never()).notifyPlayersUpdate(any());
    }
}
