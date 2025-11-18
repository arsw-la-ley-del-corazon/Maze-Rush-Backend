package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.LobbyFullException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.dto.LobbyCacheDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LobbyServiceImplTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private LobbyServiceImpl service;

    private UserEntity user1;
    private UserEntity user2;
    private LobbyEntity lobby;

    @BeforeEach
    void setup() {
        user1 = new UserEntity();
        user1.setId(UUID.randomUUID());
        user1.setUsername("Alice");

        user2 = new UserEntity();
        user2.setId(UUID.randomUUID());
        user2.setUsername("Bob");

        lobby = new LobbyEntity();
        lobby.setId(UUID.randomUUID());
        lobby.setCode("ABC123");
        lobby.setMazeSize("MEDIUM");
        lobby.setMaxPlayers(4);
        lobby.setStatus("EN_ESPERA");
        lobby.setCreatorUsername("Alice");
        lobby.addPlayer(user1);
    }

    // ------------------------------------------------------------
    // createLobby()
    // ------------------------------------------------------------
    @Test
    void testCreateLobby_OK() {

        when(userRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user1));

        when(lobbyRepository.save(any(LobbyEntity.class)))
                .thenReturn(lobby);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        LobbyEntity created = service.createLobby("MEDIUM", 4, true, null, "Alice");

        assertNotNull(created);
        verify(lobbyRepository).save(any(LobbyEntity.class));
        verify(valueOps).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCreateLobby_InvalidMaxPlayers() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createLobby("MEDIUM", 10, true, null, "Alice"));
    }

    @Test
    void testCreateLobby_UserNotFound() {
        when(userRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createLobby("MEDIUM", 4, true, null, "Alice"));
    }

    // ------------------------------------------------------------
    // getLobbyByCode()
    // ------------------------------------------------------------
    @Test
    void testGetLobbyByCode_FromRedis() {

        LobbyCacheDTO cache = new LobbyCacheDTO();
        cache.setId(lobby.getId());
        cache.setCode(lobby.getCode());
        cache.setMazeSize("MEDIUM");
        cache.setMaxPlayers(4);
        cache.setPublic(true);
        cache.setStatus("EN_ESPERA");
        cache.setCreatorUsername("Alice");
        cache.setPlayers(List.of("Alice"));

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("lobby:ABC123")).thenReturn(cache);
        when(userRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user1));

        LobbyEntity result = service.getLobbyByCode("ABC123");

        assertEquals("ABC123", result.getCode());
    }

    @Test
    void testGetLobbyByCode_FromDB() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("lobby:ABC123")).thenReturn(null);
        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        LobbyEntity result = service.getLobbyByCode("ABC123");

        assertEquals("ABC123", result.getCode());
    }

    @Test
    void testGetLobbyByCode_NotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("lobby:XXX")).thenReturn(null);
        when(lobbyRepository.findByCode("XXX")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getLobbyByCode("XXX"));
    }

    // ------------------------------------------------------------
    // joinLobbyByCode()
    // ------------------------------------------------------------
    @Test
    void testJoinLobby_OK() {

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(user2));
        when(lobbyRepository.save(any(LobbyEntity.class)))
                .thenReturn(lobby);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        LobbyEntity result = service.joinLobbyByCode("ABC123", "Bob");

        assertTrue(result.getPlayers().contains(user2));
    }

    @Test
    void testJoinLobby_LobbyNotFound() {
        when(lobbyRepository.findByCode("AAA"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.joinLobbyByCode("AAA", "Bob"));
    }

    @Test
    void testJoinLobby_UserNotFound() {
        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.joinLobbyByCode("ABC123", "Bob"));
    }

    @Test
    void testJoinLobby_AlreadyInLobby() {
        lobby.addPlayer(user2);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(user2));

        assertThrows(IllegalStateException.class,
                () -> service.joinLobbyByCode("ABC123", "Bob"));
    }

    @Test
    void testJoinLobby_LobbyFull() {
        lobby.setMaxPlayers(1);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(user2));

        assertThrows(LobbyFullException.class,
                () -> service.joinLobbyByCode("ABC123", "Bob"));
    }

    // ------------------------------------------------------------
    // leaveLobby()
    // ------------------------------------------------------------
    @Test
    void testLeaveLobby_OK() {

        lobby.addPlayer(user2);

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(user2));
        when(lobbyRepository.save(any(LobbyEntity.class)))
                .thenReturn(lobby);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        assertDoesNotThrow(() -> service.leaveLobby("ABC123", "Bob"));
    }

    @Test
    void testLeaveLobby_EmptyLobbyUpdatesStatus() {

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user1));
        when(lobbyRepository.save(any(LobbyEntity.class)))
                .thenReturn(lobby);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.leaveLobby("ABC123", "Alice");

        assertEquals("ABANDONADO", lobby.getStatus());
    }

    @Test
    void testLeaveLobby_UserNotInLobby() {

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(user2));

        assertThrows(IllegalStateException.class,
                () -> service.leaveLobby("ABC123", "Bob"));
    }

    // ------------------------------------------------------------
    // removePlayerFromLobby()
    // ------------------------------------------------------------
    @Test
    void testRemovePlayer_OK() {
        when(lobbyRepository.findById(lobby.getId()))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        assertDoesNotThrow(() ->
                service.removePlayerFromLobby(lobby.getId(), user1.getId()));
    }

    @Test
    void testRemovePlayer_LobbyNotFound() {
        UUID lobbyId = lobby.getId();
        UUID userId = user1.getId();
        when(lobbyRepository.findById(lobby.getId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.removePlayerFromLobby(lobbyId, userId));
    }

    @Test
    void testRemovePlayer_UserNotFound() {
        UUID lobbyId = lobby.getId();
        UUID userId = user1.getId();
        when(lobbyRepository.findById(lobby.getId()))
                .thenReturn(Optional.of(lobby));
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.removePlayerFromLobby(lobbyId, userId));
    }
}
