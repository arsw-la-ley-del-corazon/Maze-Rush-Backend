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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class LobbyServiceImplStartGameTest {

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

    // 1. Lobby no existe
    @Test
    void testStartGame_LobbyNotFound() {
        when(lobbyRepository.findByCode("NOPE"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.startGame("NOPE", "user"));
    }

    // Usuario NO es creador
    @Test
    void testStartGame_UserNotCreator_ThrowsException() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());

        UserEntity u1 = new UserEntity(); u1.setUsername("host");
        UserEntity u2 = new UserEntity(); u2.setUsername("invited");

        lobby.addPlayer(u1);
        lobby.addPlayer(u2);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        assertThrows(IllegalStateException.class,
                () -> service.startGame("ABC123", "invited"));
    }

    // No hay suficientes jugadores
    @Test
    void testStartGame_NotEnoughPlayers_ThrowsException() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());

        UserEntity u1 = new UserEntity(); u1.setUsername("host");
        lobby.addPlayer(u1);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        assertThrows(IllegalStateException.class,
                () -> service.startGame("ABC123", "host"));
    }

    // Caso exitoso
    @Test
    void testStartGame_Success() {

        String code = "ROOM1";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setMazeSize("10x10");  

        UserEntity host = new UserEntity(); host.setUsername("host");
        UserEntity u2   = new UserEntity(); u2.setUsername("p2");

        lobby.addPlayer(host);
        lobby.addPlayer(u2);

        org.arsw.maze_rush.maze.entities.MazeEntity maze =
                new org.arsw.maze_rush.maze.entities.MazeEntity();
        maze.setId(UUID.randomUUID());
        maze.setSize("10x10");
        maze.setWidth(10);
        maze.setHeight(10);
        maze.setLayout("{}");


        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(lobbyRepository.save(any())).thenReturn(lobby);
        when(mazeService.generateMaze("10x10")).thenReturn(maze); 

        assertDoesNotThrow(() ->
                service.startGame(code, "host"));

        verify(lobbyRepository).save(any(LobbyEntity.class));

        verify(valueOps).set(
                startsWith("lobby:"), any(), eq(1L), eq(TimeUnit.HOURS));

        verify(mazeService).generateMaze("10x10");

        verify(gameSessionManager).setMaze(eq(code), anyString());

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/" + code + "/game"),
                any(Object.class)
        );
    }


    // Excepción al enviar WS → NO rompe
    @Test
    void testStartGame_WebSocketError_DoesNotBreak() {

        String code = "ROOM2";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setCreatorUsername("host");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setMazeSize("10x10"); 

        UserEntity host = new UserEntity(); host.setUsername("host");
        UserEntity u2   = new UserEntity(); u2.setUsername("p2");

        lobby.addPlayer(host);
        lobby.addPlayer(u2);

        org.arsw.maze_rush.maze.entities.MazeEntity maze =
                new org.arsw.maze_rush.maze.entities.MazeEntity();
        maze.setId(UUID.randomUUID());

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(lobbyRepository.save(any())).thenReturn(lobby);
        when(mazeService.generateMaze("10x10")).thenReturn(maze);

        doThrow(new RuntimeException("WS error"))
                .when(messagingTemplate)
                .convertAndSend(eq("/topic/lobby/" + code + "/game"), any(Object.class));

        assertDoesNotThrow(() ->
                service.startGame(code, "host"));
    }

   
}
