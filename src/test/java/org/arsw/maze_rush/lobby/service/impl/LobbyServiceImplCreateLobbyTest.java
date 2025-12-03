package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.lobby.dto.LobbyCacheDTO;
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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class LobbyServiceImplCreateLobbyTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LobbyPlayerRepository lobbyPlayerRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private org.arsw.maze_rush.maze.service.MazeService mazeService;

    @Mock
    private org.arsw.maze_rush.game.service.GameSessionManager gameSessionManager;

    @InjectMocks
    private LobbyServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testCreateLobby_Success() {
        // -------- Arrange --------
        String creatorUsername = "sebastian";

        UserEntity creator = new UserEntity();
        creator.setId(java.util.UUID.randomUUID());
        creator.setUsername(creatorUsername);

        when(userRepository.findByUsernameIgnoreCase(creatorUsername))
                .thenReturn(Optional.of(creator));

        // Mock para evitar código duplicado
        when(lobbyRepository.findByCode(anyString()))
                .thenReturn(Optional.empty());

        LobbyEntity savedLobby = new LobbyEntity();
        savedLobby.setId(java.util.UUID.randomUUID());
        savedLobby.setCode("ABC123");
        savedLobby.setMazeSize("10x10");
        savedLobby.setStatus("EN_ESPERA");
        savedLobby.setMaxPlayers(4);
        savedLobby.setPublic(true);
        savedLobby.setCreatorUsername(creatorUsername);

        when(lobbyRepository.save(any(LobbyEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // -------- Act --------
        LobbyEntity result = service.createLobby("10x10", 4, true, "EN_ESPERA", creatorUsername);

        // -------- Assert --------
        assertNotNull(result);
        assertEquals("10x10", result.getMazeSize());
        assertEquals(4, result.getMaxPlayers());
        assertEquals("EN_ESPERA", result.getStatus());
        assertEquals(creatorUsername, result.getCreatorUsername());
        assertNotNull(result.getCode());
        assertEquals(6, result.getCode().length());

        // Verificar que se buscó el usuario
        verify(userRepository).findByUsernameIgnoreCase(creatorUsername);

        // Verificar que el lobby se guardó 2 veces: (crear + agregar jugador)
        verify(lobbyRepository, times(2)).save(any(LobbyEntity.class));

        // Verifica Redis
        verify(valueOps).set(
                startsWith("lobby:"),
                any(LobbyCacheDTO.class),
                eq(1L),
                eq(TimeUnit.HOURS)
        );

        // WS: llamada indirecta → método privado sendGlobalLobbyUpdate
        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/updates"), any(Object.class));


    }

    @Test
    void testCreateLobby_InvalidMaxPlayers_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createLobby("10x10", 10, true, "EN_ESPERA", "user")
        );
    }

    @Test
    void testCreateLobby_UserNotFound_ThrowsException() {

        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createLobby("10x10", 2, true, "EN_ESPERA", "ghost")
        );
    }
}
