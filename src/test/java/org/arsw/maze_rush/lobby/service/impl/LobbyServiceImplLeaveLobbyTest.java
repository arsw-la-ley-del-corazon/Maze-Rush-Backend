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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LobbyServiceImplLeaveLobbyTest {

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
    void testLeaveLobby_LobbyNotFound() {
        when(lobbyRepository.findByCode("NOPE"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.leaveLobby("NOPE", "user"));
    }

    // Usuario NO existe
    @Test
    void testLeaveLobby_UserNotFound() {
        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");


        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.leaveLobby("ABC123", "ghost"));
    }

    // Usuario NO pertenece → return sin excepción

    @Test
    void testLeaveLobby_UserNotInLobby_NoException() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");


        UserEntity user = new UserEntity();
        user.setUsername("user1");

        when(lobbyRepository.findByCode("ABC123")).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("user1")).thenReturn(Optional.of(user));

        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, user))
                .thenReturn(false);

        service.leaveLobby("ABC123", "user1");

        verify(lobbyRepository, never()).save(any());
    }

    // Usuario pertenece y lobby NO queda vacío
    @Test
    void testLeaveLobby_UserLeaves_LobbyNotEmpty() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setPublic(true);
        lobby.setStatus("EN_ESPERA");
        lobby.setCreatorUsername("host");
        lobby.setMaxPlayers(4);

        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");

        lobby.setLobbyPlayers(new HashSet<>());
        lobby.addPlayer(user1);
        lobby.addPlayer(user2);

        when(lobbyRepository.findByCode("ABC123")).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("user1")).thenReturn(Optional.of(user1));
        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, user1)).thenReturn(true);
        when(lobbyRepository.save(any())).thenReturn(lobby);

        service.leaveLobby("ABC123", "user1");

        verify(lobbyRepository).save(any(LobbyEntity.class));

        verify(valueOps).set(
                eq("lobby:ABC123"),
                any(),
                eq(1L),
                eq(TimeUnit.HOURS)
        );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/ABC123/players"),
                any(Object.class)
        );

        verify(messagingTemplate, never()).convertAndSend(
                eq("/topic/lobby/updates"),
                any(Object.class)
        );
    }

    

    // Usuario pertenece y lobby queda VACÍO → se elimina
    @Test
    void testLeaveLobby_LobbyBecomesEmpty_Deleted() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("BYE123");
        lobby.setPublic(true);
        lobby.setCreatorUsername("host");


        UserEntity user1 = new UserEntity();
        user1.setUsername("solo");

        lobby.setLobbyPlayers(new HashSet<>());
        lobby.addPlayer(user1);

        when(lobbyRepository.findByCode("BYE123")).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("solo")).thenReturn(Optional.of(user1));
        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, user1)).thenReturn(true);

        service.leaveLobby("BYE123", "solo");

        verify(lobbyRepository).delete(lobby);
        verify(redisTemplate).delete("lobby:BYE123");

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/BYE123"),
                any(Map.class)
        );
    }
}
