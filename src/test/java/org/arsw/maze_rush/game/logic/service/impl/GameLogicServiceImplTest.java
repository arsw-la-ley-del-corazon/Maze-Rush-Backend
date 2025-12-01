package org.arsw.maze_rush.game.logic.service.impl;

import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameLogicServiceImplTest {

    @Mock private GameRepository gameRepository;
    @Mock private PowerUpService powerUpService;
    @Mock private GameSessionManager gameSessionManager; 

    @InjectMocks
    private GameLogicServiceImpl service;

    private UUID gameId;
    private GameEntity gameEntity;
    private MazeEntity mazeEntity;
    private final String lobbyCode = "TEST_LOBBY";
    private final String username = "player1";

    @BeforeEach
    void setup() {
        gameId = UUID.randomUUID();

        mazeEntity = new MazeEntity();
        mazeEntity.setLayout("000\n000\n000"); 
        mazeEntity.setWidth(3);
        mazeEntity.setHeight(3);
        mazeEntity.setStartX(0);
        mazeEntity.setStartY(0);

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(lobbyCode);
        lobby.setMaze(mazeEntity);

        gameEntity = new GameEntity();
        gameEntity.setId(gameId);
        gameEntity.setLobby(lobby);
        gameEntity.setPlayers(Set.of(fakeUser(username)));
    }

    // TESTS DE INICIALIZACIÓN
    @Test
    void initializeGame_ShouldInitializeCorrectly() {
        UUID id = UUID.randomUUID();
        MazeEntity maze = fakeMaze();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode("LOBBY_TEST");
        lobby.setMaze(maze);

        GameEntity game = new GameEntity();
        game.setId(id);
        game.setLobby(lobby); 
        game.setMaze(maze);   
        game.setPlayers(new HashSet<>(Set.of(fakeUser("player1"))));

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        List<PowerUp> generated = List.of(
                PowerUp.builder().x(2).y(2).build()
        );

        when(powerUpService.generatePowerUps(any(MazeEntity.class))).thenReturn(generated);

        GameState st = service.initializeGame(id);

        assertNotNull(st);
        assertEquals("EN_CURSO", st.getStatus());
        assertEquals(id, st.getGameId());
        
        verify(gameSessionManager).setPowerUps("LOBBY_TEST",generated);
        verify(gameRepository).save(game);
    }

    // TESTS DE MOVIMIENTO BÁSICO
    @Test
    void movePlayer_ShouldMoveCorrectly() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        PlayerGameStateDTO playerState = new PlayerGameStateDTO();
        playerState.setUsername(username);
        playerState.setPosition(new PositionDTO(1, 1));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername(username);
        req.setDirection("UP");

        service.movePlayer(gameId, req);

        ArgumentCaptor<PositionDTO> captor = ArgumentCaptor.forClass(PositionDTO.class);
        verify(gameSessionManager).updatePlayerPosition(eq(lobbyCode), eq(username), captor.capture());
        
        // (1,1) -> UP -> (1,0)
        assertEquals(1, captor.getValue().getX());
        assertEquals(0, captor.getValue().getY());
    }

    // TESTS EFECTOS TEMPORALES
    @Test
    void movePlayer_WhenFrozen_ShouldNotMove() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));

        PlayerGameStateDTO playerState = new PlayerGameStateDTO();
        playerState.setUsername(username);
        playerState.setPosition(new PositionDTO(1, 1));
        
        // ** FREEZE **
        playerState.getActiveEffects().put(PowerUpType.FREEZE, Instant.now().plusSeconds(5).toEpochMilli());

        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername(username);
        req.setDirection("UP");

        service.movePlayer(gameId, req);

        verify(gameSessionManager, never()).updatePlayerPosition(anyString(), anyString(), any());
        
        verify(gameSessionManager).cleanExpiredEffects(lobbyCode, username);
    }

    @Test
    void movePlayer_WhenConfused_ShouldInvertControls() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));

        PlayerGameStateDTO playerState = new PlayerGameStateDTO();
        playerState.setUsername(username);
        playerState.setPosition(new PositionDTO(1, 1)); // Centro

        // ** CONFUSION **
        playerState.getActiveEffects().put(PowerUpType.CONFUSION, Instant.now().plusSeconds(5).toEpochMilli());

        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername(username);
        req.setDirection("UP");

        service.movePlayer(gameId, req);

        ArgumentCaptor<PositionDTO> positionCaptor = ArgumentCaptor.forClass(PositionDTO.class);
        verify(gameSessionManager).updatePlayerPosition(eq(lobbyCode), eq(username), positionCaptor.capture());
        
        PositionDTO newPos = positionCaptor.getValue();
        
        // Verificamos lógica invertida: (1,1) -> UP (invertido a DOWN) -> (1,2)
        assertEquals(1, newPos.getX());
        assertEquals(2, newPos.getY(), "Con confusión, UP debe actuar como DOWN");
    }

    // TESTS APLICACIÓN DE PODERES AL RECOGER
    @Test
    void movePlayer_CollectClearFog_ShouldApplyToSelf() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        PlayerGameStateDTO playerState = new PlayerGameStateDTO(username, new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PowerUp powerUp = PowerUp.builder().type(PowerUpType.CLEAR_FOG).duration(5).x(1).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0)).thenReturn(powerUp);

         PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername(username);
        req.setDirection("RIGHT");

        service.movePlayer(gameId, req);

        verify(gameSessionManager).applyEffect(lobbyCode,username, PowerUpType.CLEAR_FOG, 5);
    }

    @Test
    void movePlayer_CollectFreeze_ShouldApplyToOpponents() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        PlayerGameStateDTO playerState = new PlayerGameStateDTO(username, new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PowerUp powerUp = PowerUp.builder().type(PowerUpType.FREEZE).duration(3).x(1).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0)).thenReturn(powerUp);

        PlayerMoveRequestDTO req = new PlayerMoveRequestDTO();
        req.setUsername(username);
        req.setDirection("RIGHT");

        service.movePlayer(gameId, req);

        verify(gameSessionManager).applyEffectToOpponents(lobbyCode,username,PowerUpType.FREEZE,3);
    }


    private UserEntity fakeUser(String username) {
        UserEntity u = new UserEntity();
        u.setUsername(username);
        return u;
    }

    private MazeEntity fakeMaze() {
        MazeEntity m = new MazeEntity();
        m.setWidth(5);
        m.setHeight(5);
        m.setStartX(0);
        m.setStartY(0);
        m.setGoalX(4);
        m.setGoalY(4);
        m.setLayout("00000\n00000\n00000\n00000\n00000");
        return m;
    }


}