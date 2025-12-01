package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.dto.LobbyCacheDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyPlayerRepository;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyServiceImplGetLobbyByCodeTest {

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

    //  TEST: CACHE HIT  

    @Test
    void testGetLobbyByCode_ReturnsFromRedis() {
        String code = "ABC123";

        LobbyCacheDTO cached = new LobbyCacheDTO();
        cached.setId(UUID.randomUUID());
        cached.setCode(code);
        cached.setMazeSize("MEDIUM");
        cached.setMaxPlayers(4);
        cached.setPublic(true);
        cached.setStatus("EN_ESPERA");
        cached.setCreatorUsername("creator");

        when(valueOps.get("lobby:" + code)).thenReturn(cached);

        LobbyEntity result = service.getLobbyByCode(code);

        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals("MEDIUM", result.getMazeSize());
        assertEquals(4, result.getMaxPlayers());
        assertEquals("EN_ESPERA", result.getStatus());
        assertEquals("creator", result.getCreatorUsername());

        verify(lobbyRepository, never()).findByCode(anyString());
    }

    // TEST: DATABASE HIT

    @Test
    void testGetLobbyByCode_ReturnsFromDatabaseWhenNotInRedis() {
        String code = "XYZ999";

        when(valueOps.get("lobby:" + code)).thenReturn(null);

        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(UUID.randomUUID());
        lobby.setCode(code);
        lobby.setMazeSize("10x10");
        lobby.setMaxPlayers(3);
        lobby.setStatus("EN_ESPERA");
        lobby.setPublic(true);
        lobby.setCreatorUsername("creatorUser");

        when(lobbyRepository.findByCode(code)).thenReturn(Optional.of(lobby));

        LobbyEntity result = service.getLobbyByCode(code);

        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals("10x10", result.getMazeSize());
        assertEquals(3, result.getMaxPlayers());
        assertEquals("creatorUser", result.getCreatorUsername());
    }

    // TEST: LOBBY NO EXISTE → EXCEPCIÓN 

    @Test
    void testGetLobbyByCode_NotFound_ThrowsException() {
        String code = "NOT_EXIST";

        when(valueOps.get("lobby:" + code)).thenReturn(null);
        when(lobbyRepository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.getLobbyByCode(code)
        );

        verify(lobbyRepository).findByCode(code);
    }
}
