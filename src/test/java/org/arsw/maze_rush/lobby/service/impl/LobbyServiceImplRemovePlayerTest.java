package org.arsw.maze_rush.lobby.service.impl;

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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LobbyServiceImplRemovePlayerTest {

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
    void testRemovePlayer_LobbyNotFound() {

        UUID lobbyId = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();

        when(lobbyRepository.findById(lobbyId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.removePlayerFromLobby(lobbyId, userId));
    }

    //  Usuario NO existe
    @Test
    void testRemovePlayer_UserNotFound() {

        UUID lobbyId = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(lobbyId);
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");

        when(lobbyRepository.findById(lobbyId))
                .thenReturn(Optional.of(lobby));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.removePlayerFromLobby(lobbyId, userId));
    }

    // Lobby queda vacío → delete + deleteRedis
    @Test
    void testRemovePlayer_LobbyBecomesEmpty() {

        UUID lobbyId = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(lobbyId);
        lobby.setCode("XYZ111");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("juan");

        lobby.addPlayer(user);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removePlayerFromLobby(lobbyId, userId);

        verify(lobbyRepository).delete(lobby);

        verify(redisTemplate).delete("lobby:" + lobby.getCode());
    }

    // Lobby NO queda vacío → save + saveRedis
    @Test
    void testRemovePlayer_LobbyStillHasPlayers() {

        UUID lobbyId = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(lobbyId);
        lobby.setCode("XYZ222");
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());

        UserEntity u1 = new UserEntity(); u1.setId(UUID.randomUUID()); u1.setUsername("a");
        UserEntity u2 = new UserEntity(); u2.setId(userId);          u2.setUsername("b");

        lobby.addPlayer(u1);
        lobby.addPlayer(u2);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u2));

        service.removePlayerFromLobby(lobbyId, userId);

        verify(lobbyRepository).save(lobby);

        verify(valueOps).set(
                eq("lobby:" + lobby.getCode()),
                any(),
                eq(1L),
                eq(TimeUnit.HOURS)
        );
    }
}
