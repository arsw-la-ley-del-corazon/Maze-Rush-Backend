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
import static org.mockito.Mockito.*;

class LobbyServiceImplToggleReadyTest {

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

    // Lobby no existe
    @Test
    void testToggleReady_LobbyNotFound() {
        when(lobbyRepository.findByCode("XXX"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleReady("XXX", "user"));
    }

    // Usuario no existe
    @Test
    void testToggleReady_UserNotFound() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setCreatorUsername("host");


        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.toggleReady("ABC123", "ghost"));
    }

    // Usuario no pertenece al lobby
    @Test
    void testToggleReady_UserNotInLobby_ThrowsException() {

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("ABC123");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");


        UserEntity user = new UserEntity();
        user.setUsername("juan");

        when(lobbyRepository.findByCode("ABC123")).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> service.toggleReady("ABC123", "juan"));
    }

    // Estado no existe en Redis → debe volverse TRUE
    @Test
    void testToggleReady_ShouldBecomeTrue_WhenNoPreviousState() {

        String code = "ABC123";
        String username = "juan";
        String redisKey = "lobby:" + code + ":ready:" + username;

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");


        UserEntity user = new UserEntity();
        user.setUsername(username);

        lobby.addPlayer(user); 

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(valueOps.get(redisKey)).thenReturn(null); 

        boolean result = service.toggleReady(code, username);

        assertTrue(result);

        verify(valueOps).set(redisKey, true, 1L, TimeUnit.HOURS);
    }

    // Estado existe y es TRUE → debe volverse FALSE
    @Test
    void testToggleReady_ShouldBecomeFalse_WhenWasTrue() {

        String code = "ABC123";
        String username = "juan";
        String redisKey = "lobby:" + code + ":ready:" + username;

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.setCreatorUsername("host");


        UserEntity user = new UserEntity();
        user.setUsername(username);

        lobby.addPlayer(user);

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(valueOps.get(redisKey)).thenReturn(true);

        boolean result = service.toggleReady(code, username);

        assertFalse(result);

        verify(valueOps).set(redisKey, false, 1L, TimeUnit.HOURS);
    }
}
