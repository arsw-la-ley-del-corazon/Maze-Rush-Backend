package org.arsw.maze_rush.game.logic.service.impl;

import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.users.entities.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameLogicServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameLogicServiceImpl service;

    private final UUID gameID = UUID.randomUUID();
    private final UUID otherID = UUID.randomUUID();
    private final String username = "testUser";

    @Mock private GameEntity mockGame;
    @Mock private MazeEntity mockMaze;
    @Mock private UserEntity mockUser;
    @Mock private LobbyEntity mockLobby;

    // Utilidad para insertar en cache
    private void initCache(UUID id) {
        GameState state = new GameState();
        state.setGameId(id);
        state.setStatus("EN_CURSO");
        state.setPlayerPositions(List.of(new PlayerPosition(mockUser, 5, 5, 0)));

        try {
            var field = GameLogicServiceImpl.class.getDeclaredField("activeGames");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, GameState> map = (ConcurrentHashMap<UUID, GameState>) field.get(service);
            map.put(id, state);
        } catch (Exception e) {
            fail("Error inicializando cache");
        }
    }

    @BeforeEach
    void clear() {
    }

    // initializeGame(UUID id)

    @Test
    void initializeGame_Successful() {
        when(gameRepository.findById(otherID))
                .thenReturn(Optional.of(mockGame));

        when(mockGame.getMaze()).thenReturn(mockMaze);
        when(mockGame.getPlayers()).thenReturn(Set.of(mockUser));

        when(mockMaze.getStartX()).thenReturn(1);
        when(mockMaze.getStartY()).thenReturn(2);

        GameState result = service.initializeGame(otherID);

        assertNotNull(result);
        assertEquals(otherID, result.getGameId());
    }

    @Test
    void initializeGame_NotFound() {
        when(gameRepository.findById(gameID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.initializeGame(gameID));
    }

    @Test
    void initializeGame_NoMaze() {
        when(gameRepository.findById(gameID)).thenReturn(Optional.of(mockGame));
        when(mockGame.getMaze()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> service.initializeGame(gameID));
    }

    @Test
    void initializeGame_NoPlayers() {
        when(gameRepository.findById(gameID)).thenReturn(Optional.of(mockGame));
        when(mockGame.getMaze()).thenReturn(mockMaze);
        when(mockGame.getPlayers()).thenReturn(Collections.emptySet());

        assertThrows(IllegalStateException.class,
                () -> service.initializeGame(gameID));
    }

    // movePlayer(...)

    @Test
    void movePlayer_GameNotActive() {
        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        assertThrows(IllegalStateException.class,
                () -> service.movePlayer(gameID, dto));
    }

    @Test
    void movePlayer_PlayerNotFound() {
        initCache(gameID);

        when(mockUser.getUsername()).thenReturn("testUser");

        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername("otroUsuario");

        assertThrows(IllegalArgumentException.class,
                () -> service.movePlayer(gameID, dto));
    }


    @Test
    void movePlayer_InvalidDirection() {
        initCache(gameID);
        when(mockUser.getUsername()).thenReturn(username);

        when(gameRepository.findById(gameID))
                .thenReturn(Optional.of(mockGame));
        when(mockGame.getLobby()).thenReturn(mockLobby);
        when(mockLobby.getMaze()).thenReturn(mockMaze);
        when(mockMaze.getLayout()).thenReturn("...\n...\n...");

        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername(username);
        dto.setDirection("JUMP");

        assertThrows(IllegalArgumentException.class,
                () -> service.movePlayer(gameID, dto));
    }

    @Test
    void movePlayer_Up_Success() {
        initCache(gameID);
        when(mockUser.getUsername()).thenReturn(username);

        when(gameRepository.findById(gameID))
                .thenReturn(Optional.of(mockGame));
        when(mockGame.getLobby()).thenReturn(mockLobby);
        when(mockLobby.getMaze()).thenReturn(mockMaze);

        when(mockMaze.getLayout()).thenReturn("...\n.X.\n...");
        PlayerPosition p = service.getCurrentState(gameID)
                .getPlayerPositions().get(0);

        p.setX(1);
        p.setY(1);

        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername(username);
        dto.setDirection("UP");

        service.movePlayer(gameID, dto);

        assertEquals(1, p.getX());
        assertEquals(0, p.getY());
    }

    @Test
    void movePlayer_CollisionWithWall() {
        initCache(gameID);
        when(mockUser.getUsername()).thenReturn(username);

        when(gameRepository.findById(gameID))
                .thenReturn(Optional.of(mockGame));
        when(mockGame.getLobby()).thenReturn(mockLobby);
        when(mockLobby.getMaze()).thenReturn(mockMaze);
        when(mockMaze.getLayout()).thenReturn("...\n.#.\n...");

        PlayerPosition p = service.getCurrentState(gameID)
                .getPlayerPositions().get(0);

        p.setX(0);
        p.setY(1);

        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername(username);
        dto.setDirection("RIGHT");

        assertThrows(IllegalStateException.class,
                () -> service.movePlayer(gameID, dto));

        assertEquals(0, p.getX());
        assertEquals(1, p.getY());
    }

    @Test
    void movePlayer_OutOfBounds() {
        initCache(gameID);
        when(mockUser.getUsername()).thenReturn(username);

        when(gameRepository.findById(gameID))
                .thenReturn(Optional.of(mockGame));
        when(mockGame.getLobby()).thenReturn(mockLobby);
        when(mockLobby.getMaze()).thenReturn(mockMaze);
        when(mockMaze.getLayout()).thenReturn("...\n...\n...");

        PlayerPosition p = service.getCurrentState(gameID)
                .getPlayerPositions().get(0);

        p.setX(0);
        p.setY(1);

        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername(username);
        dto.setDirection("LEFT");

        assertThrows(IllegalStateException.class,
                () -> service.movePlayer(gameID, dto));
    }

    // getCurrentState(...)

    @Test
    void getCurrentState_Found() {
        initCache(gameID);
        GameState s = service.getCurrentState(gameID);
        assertNotNull(s);
        assertEquals(gameID, s.getGameId());
    }

    @Test
    void getCurrentState_NotFound() {
        assertNull(service.getCurrentState(otherID));
    }
}
