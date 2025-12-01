package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
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
import static org.mockito.Mockito.*;

class LobbyServiceImplSendChatMessageTest {

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
    void testSendChatMessage_LobbyNotFound() {
        when(lobbyRepository.findByCode("XXX"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.sendChatMessage("XXX", "user", "hola"));
    }

    // Usuario NO existe
    @Test
    void testSendChatMessage_UserNotFound() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");


        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.sendChatMessage("ABC123", "ghost", "hola"));
    }

    // Usuario NO pertenece al lobby
    @Test
    void testSendChatMessage_UserNotInLobby_ThrowsException() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");

        UserEntity user = new UserEntity();
        user.setUsername("juan");

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("juan"))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> service.sendChatMessage("ABC123", "juan", "hola"));
    }

    // Caso exitoso → NO lanza excepción
    @Test
    void testSendChatMessage_Success() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");

        UserEntity user = new UserEntity();
        user.setUsername("juan");
        
        lobby.addPlayer(user);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("juan"))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() ->
                service.sendChatMessage("ABC123", "juan", "hola")
        );
    }
}
