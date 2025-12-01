package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.LobbyFullException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class LobbyServiceImplJoinLobbyTest {

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

    // TEST: Unirse correctamente a un lobby público
    @Test
    void testJoinLobby_SuccessPublicLobby() {

        String code = "ABC123";
        String username = "sebastian";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(UUID.randomUUID());
        lobby.setCode(code);
        lobby.setMaxPlayers(4);
        lobby.setPublic(true);
        lobby.setStatus("EN_ESPERA");
        lobby.setCreatorUsername("hostUser");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);

        lobby.setLobbyPlayers(new HashSet<>());

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)).thenReturn(false);
        when(lobbyRepository.save(any())).thenReturn(lobby);

        LobbyEntity result = service.joinLobbyByCode(code, username);

        assertNotNull(result);
        verify(lobbyRepository).findByCode(code);
        verify(userRepository).findByUsernameIgnoreCase(username);
        verify(lobbyRepository).save(any(LobbyEntity.class));

        verify(valueOps).set(startsWith("lobby:"), any(), eq(1L), eq(TimeUnit.HOURS));

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/" + code + "/players"),
                any(PlayerEventDTO.class)
        );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/lobby/updates"),
                any(Object.class)
        );
    }

    // TEST: Usuario ya está en el lobby
    @Test
    void testJoinLobby_UserAlreadyInLobby_ThrowsException() {
        String code = "ABC123";
        String username = "playerX";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        UserEntity user = new UserEntity();
        user.setUsername(username);

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.joinLobbyByCode(code, username));
    }

    // TEST: Lobby está lleno → LobbyFullException
    @Test
    void testJoinLobby_LobbyFull_ThrowsException() {

        String code = "FULLX";
        String username = "user1";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setMaxPlayers(1);
        lobby.setCreatorUsername("hostUser");

        UserEntity existing = new UserEntity();
        existing.setUsername("aaa");
        lobby.setLobbyPlayers(new HashSet<>());
        lobby.addPlayer(existing);
        


        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(newUser));
        when(lobbyPlayerRepository.existsByLobbyAndUser(lobby, newUser)).thenReturn(false);

        assertThrows(LobbyFullException.class,
                () -> service.joinLobbyByCode(code, username));
    }

    // TEST: Lobby no existe → NotFoundException
    @Test
    void testJoinLobby_LobbyNotFound_ThrowsException() {
        when(lobbyRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.joinLobbyByCode("NOPE", "user"));
    }

    // TEST: Usuario no existe → NotFoundException
    @Test
    void testJoinLobby_UserNotFound_ThrowsException() {
        String code = "ABC777";

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("xxx")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.joinLobbyByCode(code, "xxx"));
    }
}
