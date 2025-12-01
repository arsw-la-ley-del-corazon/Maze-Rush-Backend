package org.arsw.maze_rush.game.controller;

import org.arsw.maze_rush.game.dto.*;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameWebSocketControllerTest {

    @Mock
    private GameSessionManager sessionManager;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Principal principal;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private GameWebSocketController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(principal.getName()).thenReturn("player1");

        Map<String, Object> sessionMap = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionMap);
    }

    // handlePlayerJoin
    @Test
    void testHandlePlayerJoin_ok() {

        Map<String, String> payload = Map.of("username", "player1");

        controller.handlePlayerJoin("ABC123", payload, principal, headerAccessor);

        verify(sessionManager).addPlayer("ABC123", "player1");

        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/ABC123/move"),
            argThat((GameEventDTO ev) ->
                    ev.getType().equals("player_joined") &&
                    ev.getUsername().equals("player1")
            )
        );

    }

    // handlePlayerMove
    @Test
    void testHandlePlayerMove_ok() {

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "player1");
        payload.put("position", Map.of("x", 5, "y", 7));

        controller.handlePlayerMove("L1", payload, principal);

        verify(sessionManager).updatePlayerPosition(eq("L1"), eq("player1"), any(PositionDTO.class));

        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/L1/move"),
            argThat((GameMoveDTO ev) -> ev.getUsername().equals("player1"))
        );

    }

    // handlePlayerFinish
    @Test
    void testHandlePlayerFinish_ok() {

        Map<String, Object> payload = Map.of("username", "player1");

        PositionDTO pos = new PositionDTO(0, 0);
        PlayerGameStateDTO mockState = new PlayerGameStateDTO("player1", pos);
        mockState.setIsFinished(true);
        mockState.setFinishTime(20L);

        when(sessionManager.getPlayer("L1", "player1")).thenReturn(mockState);
        when(sessionManager.getPlayers("L1")).thenReturn(List.of(mockState));

        controller.handlePlayerFinish("L1", payload, principal);

        verify(sessionManager).markPlayerFinished("L1", "player1");

        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/L1/move"),
            argThat((GameFinishDTO ev) ->
                    ev.getUsername().equals("player1")
            )
        );

    }

    // TEST: handlePlayerLeave
    @Test
    void testHandlePlayerLeave_ok() {

        Map<String, String> payload = Map.of("username", "player1");

        controller.handlePlayerLeave("L55", payload, principal);

        verify(sessionManager).removePlayer("L55", "player1");

        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/L55/move"),
            argThat((GameEventDTO ev) ->
                    ev.getType().equals("player_left") &&
                    ev.getUsername().equals("player1")
            )
        );
    }

    // TEST: extractUsername
    @Test
    void testExtractUsername_fromPayload() {
        Map<String, Object> payload = Map.of("username", "Sebastian");

        String result = invokeExtractUsername(payload, null);

        assertEquals("Sebastian", result);
    }

    @Test
    void testExtractUsername_fromPrincipal() {
        Map<String, Object> payload = Map.of();

        when(principal.getName()).thenReturn("PrincipalUser");

        String result = invokeExtractUsername(payload, principal);

        assertEquals("PrincipalUser", result);
    }

    @Test
    void testExtractUsername_returnsNullWhenMissing() {
        Map<String, Object> payload = Map.of();

        String result = invokeExtractUsername(payload, null);

        assertNull(result);
    }

    private String invokeExtractUsername(Map<String, ?> payload, Principal pr) {
        try {
            var method = GameWebSocketController.class
                    .getDeclaredMethod("extractUsername", Map.class, Principal.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, payload, pr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
