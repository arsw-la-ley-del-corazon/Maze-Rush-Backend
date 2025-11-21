package org.arsw.maze_rush.game.logic.service.impl;

import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.arsw.maze_rush.users.entities.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameLogicServiceImplTest {

    private GameRepository gameRepository;
    private PowerUpService powerUpService;
    private GameLogicServiceImpl service;

    private Map<UUID, GameState> activeGames;

    @BeforeEach
    void setup() {
        gameRepository = mock(GameRepository.class);
        powerUpService = mock(PowerUpService.class);

        service = new GameLogicServiceImpl(gameRepository, powerUpService);

        activeGames = (Map<UUID, GameState>) getPrivateField(service, "activeGames");
        activeGames.clear();
    }


    @Test
    void initializeGame_ShouldThrow_WhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.initializeGame(id));
    }

    @Test
    void initializeGame_ShouldThrow_WhenMazeNull() {
        UUID id = UUID.randomUUID();

        GameEntity game = new GameEntity();
        game.setMaze(null);
        game.setPlayers(new HashSet<>(Set.of(fakeUser("test"))));

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> service.initializeGame(id));
    }


    @Test
    void initializeGame_ShouldThrow_WhenPlayersEmpty() {
        UUID id = UUID.randomUUID();

        GameEntity game = new GameEntity();
        game.setMaze(fakeMaze());
        game.setPlayers(new HashSet<>());

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> service.initializeGame(id));
    }


    @Test
    void initializeGame_ShouldInitializeCorrectly() {

        UUID id = UUID.randomUUID();

        GameEntity game = new GameEntity();
        game.setMaze(fakeMaze());
        game.setPlayers(new HashSet<>(Set.of(fakeUser("player1"))));

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        List<PowerUp> generated = List.of(
                PowerUp.builder().x(2).y(2).build()
        );

        when(powerUpService.generatePowerUps(any(), any())).thenReturn(generated);

        GameState st = service.initializeGame(id);

        assertNotNull(st);
        assertEquals("EN_CURSO", st.getStatus());
        assertEquals(id, st.getGameId());
        assertEquals(1, st.getPlayerPositions().size());
        assertEquals(1, st.getPowerUps().size());

        verify(gameRepository, times(1)).save(game);
    }


    @Test
    void movePlayer_ShouldThrow_WhenGameNotActive() {
        UUID gameId = UUID.randomUUID();
        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername("p1");
        req.setDirection("UP");

        assertThrows(IllegalStateException.class, () -> service.movePlayer(gameId, req));
    }


    @Test
    void movePlayer_ShouldThrow_WhenPlayerMissing() {

        UUID gameId = UUID.randomUUID();

        GameState st = new GameState();
        st.setPlayerPositions(new ArrayList<>());
        activeGames.put(gameId, st);

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername("p1");
        req.setDirection("UP");

        assertThrows(IllegalArgumentException.class, () -> service.movePlayer(gameId, req));
    }


    @Test
    void movePlayer_ShouldThrow_WhenOutOfBounds() {

        UUID gameId = UUID.randomUUID();

        PlayerPosition pos = new PlayerPosition(fakeUser("p1"), 0, 0, 0);

        GameState st = new GameState();
        st.setPlayerPositions(List.of(pos));
        activeGames.put(gameId, st);

        GameEntity game = fakeGameWithLayout("""
                000
                000
                000
                """);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername("p1");
        req.setDirection("UP");

        assertThrows(IllegalStateException.class, () -> service.movePlayer(gameId, req));
    }


    @Test
    void movePlayer_ShouldMoveCorrectly() {

        UUID gameId = UUID.randomUUID();

        PlayerPosition pos = new PlayerPosition(fakeUser("p1"), 1, 1, 0);

        GameState st = new GameState();
        st.setPlayerPositions(List.of(pos));
        activeGames.put(gameId, st);

        GameEntity game = fakeGameWithLayout("""
                000
                000
                000
                """);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername("p1");
        req.setDirection("UP");

        GameState result = service.movePlayer(gameId, req);

        PlayerPosition updated = result.getPlayerPositions().get(0);

        assertEquals(1, updated.getX());
        assertEquals(0, updated.getY());
    }



    private UserEntity fakeUser(String username) {
        UserEntity u = new UserEntity();
        u.setUsername(username);
        return u;
    }

    private MazeEntity fakeMaze() {
        return MazeEntity.builder()
                .width(5)
                .height(5)
                .startX(0)
                .startY(0)
                .goalX(4)
                .goalY(4)
                .layout("""
                        00000
                        00000
                        00000
                        00000
                        00000
                        """)
                .build();
    }


    private GameEntity fakeGameWithLayout(String layout) {
        int height = layout.split("\n").length;
        int width = layout.split("\n")[0].length();

        MazeEntity maze = MazeEntity.builder()
                .width(width)
                .height(height)
                .startX(1)
                .startY(1)
                .goalX(2)
                .goalY(2)
                .layout(layout)
                .build();

        GameEntity g = new GameEntity();
        g.setMaze(maze);
        g.setLobby(fakeLobbyWithMaze(maze)); 
        g.setPlayers(Set.of(fakeUser("p1")));
        return g;
    }


    private Object getPrivateField(Object obj, String field) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LobbyEntity fakeLobbyWithMaze(MazeEntity maze) {
        LobbyEntity lobby = new LobbyEntity();
        lobby.setMaze(maze);
        return lobby;
    }

}
