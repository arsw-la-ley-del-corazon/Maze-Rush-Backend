package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.lobby.dto.PlayerEventDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyPlayerRepository;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LobbyServiceImplNotifyPlayersTest {

    @Mock private LobbyRepository lobbyRepository;
    @Mock private UserRepository userRepository;
    @Mock private LobbyPlayerRepository lobbyPlayerRepository;
    @Mock private RedisTemplate<String,Object> redisTemplate;
    @Mock private ValueOperations<String,Object> valueOps;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private org.arsw.maze_rush.maze.service.MazeService mazeService;
    @Mock private org.arsw.maze_rush.game.service.GameSessionManager gameSessionManager;

    @InjectMocks
    private LobbyServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // Lobby NO existe
    @Test
    void testNotifyPlayersUpdate_LobbyNotFound() {

        when(lobbyRepository.findByCode("NOPE"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.notifyPlayersUpdate("NOPE"));

        verify(messagingTemplate, never())
                .convertAndSend(anyString(), any(Object.class));
    }


    // Caso exitoso
    @Test
    void testNotifyPlayersUpdate_Success() {

        String code = "L1";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setMaxPlayers(4);
        lobby.setStatus("EN_ESPERA");   
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());

        UserEntity u1 = new UserEntity();
        u1.setUsername("juan");

        UserEntity u2 = new UserEntity();
        u2.setUsername("pablo");

        lobby.addPlayer(u1);
        lobby.addPlayer(u2);

        when(lobbyRepository.findByCode(code))
                .thenReturn(Optional.of(lobby));

        assertDoesNotThrow(() -> service.notifyPlayersUpdate(code));

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/" + code + "/players"),
                any(PlayerEventDTO.class)
        );
    }

    // Excepción durante WS → NO rompe
    @Test
    void testNotifyPlayersUpdate_WebSocketError_DoesNotBreak() {

        String code = "L2";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setMaxPlayers(3);
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());

        UserEntity u1 = new UserEntity(); u1.setUsername("a");
        UserEntity u2 = new UserEntity(); u2.setUsername("b");

        lobby.addPlayer(u1);
        lobby.addPlayer(u2);

        when(lobbyRepository.findByCode(code))
                .thenReturn(Optional.of(lobby));

        doThrow(new RuntimeException("WS error"))
                .when(messagingTemplate)
                .convertAndSend(eq("/topic/lobby/" + code + "/players"),
                        any(Object.class));

        assertDoesNotThrow(() -> service.notifyPlayersUpdate(code));
    }
}
