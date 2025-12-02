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
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
    @Mock private SimpMessagingTemplate messagingTemplate;

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

    @Test
    void movePlayer_CollectFreeze_ShouldNotifyAndApply() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        PlayerGameStateDTO playerState = new PlayerGameStateDTO();
        playerState.setUsername(username);
        playerState.setPosition(new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);
        // Simulamos recoger FREEZE
        PowerUp powerUp = PowerUp.builder().type(PowerUpType.FREEZE).duration(3).x(1).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0)).thenReturn(powerUp);

        service.movePlayer(gameId, new PlayerMoveRequestDTO(username, "RIGHT"));

        // Efecto aplicado (Lógica)
        verify(gameSessionManager).applyEffectToOpponents(lobbyCode, username, PowerUpType.FREEZE, 3);

        // Notificación enviada (UX)
        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/" + lobbyCode + "/notifications"),
            any(org.arsw.maze_rush.game.dto.GameNotificationDTO.class)
        );
    }

    @Test
    void movePlayer_CollectMultiplePowerUps_ShouldStackDuration() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        PlayerGameStateDTO playerState = new PlayerGameStateDTO(username, new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        // Recoger primer ClearFog (5s) en (1,0)
        PowerUp p1 = PowerUp.builder().type(PowerUpType.CLEAR_FOG).duration(5).x(1).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0)).thenReturn(p1);

        service.movePlayer(gameId, new PlayerMoveRequestDTO(username, "RIGHT"));
        
        verify(gameSessionManager).applyEffect(lobbyCode,username,PowerUpType.CLEAR_FOG, 5);

        // Moverse a (2,0) y recoger OTRO ClearFog (5s)
        playerState.setPosition(new PositionDTO(1, 0));
        PowerUp p2 = PowerUp.builder().type(PowerUpType.CLEAR_FOG).duration(5).x(2).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 2, 0)).thenReturn(p2);
        
        service.movePlayer(gameId, new PlayerMoveRequestDTO(username, "RIGHT"));

        verify(gameSessionManager, times(2)).applyEffect(lobbyCode,username,PowerUpType.CLEAR_FOG, 5);
    }

    /**Validación de Power-Up Fantasma.
     * 1. Player 1 se mueve a (1,0) y recoge 'CLEAR_FOG'.
     * 2. Player 2 se mueve a (1,0) inmediatamente después.
     * 3. Player 2 NO debe recoger nada 
     */
    @Test
    void movePlayer_PhantomPowerUp_ShouldNotBeCollectedTwice() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        // Estado Player 1 (en 0,0)
        PlayerGameStateDTO p1State = new PlayerGameStateDTO("player1", new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, "player1")).thenReturn(p1State);
        
        // Estado Player 2 (en 2,0)
        PlayerGameStateDTO p2State = new PlayerGameStateDTO("player2", new PositionDTO(2, 0));
        when(gameSessionManager.getPlayer(lobbyCode, "player2")).thenReturn(p2State);

        PowerUp powerUp = PowerUp.builder()
                .type(PowerUpType.CLEAR_FOG)
                .duration(5)
                .x(1).y(0)
                .build();

        // Player 1 -> Devuelve el item.
        // Player 2 -> Devuelve null (ya se borró).
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0))
                .thenReturn(powerUp)  
                .thenReturn(null);    

        //  Player 1 recoge el ítem ---
        service.movePlayer(gameId, new PlayerMoveRequestDTO("player1", "RIGHT")); // Va a (1,0)

        // Se aplicó el efecto a Player 1
        verify(gameSessionManager).applyEffect(
            lobbyCode, "player1", PowerUpType.CLEAR_FOG, 5);
            
        // Se guardó en DB 
        verify(gameRepository, times(1)).save(gameEntity);


        // Player 2 pasa por la misma casilla 
        service.movePlayer(gameId, new PlayerMoveRequestDTO("player2", "LEFT")); 

        // NO se aplicó ningún efecto a Player 2
        verify(gameSessionManager, never()).applyEffect(
            eq(lobbyCode), eq("player2"), any(), anyInt());
        
        // No se debió guardar en DB una segunda vez 
        verify(gameRepository, times(1)).save(gameEntity);
    }

    /**
     * Verificar que el estado retornado incluye el layout actualizado.
     */
    @Test
    void movePlayer_ShouldReturnUpdatedLayoutInGameState() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        
        PlayerGameStateDTO playerState = new PlayerGameStateDTO(username, new PositionDTO(0, 0));
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);

        PowerUp p = PowerUp.builder().type(PowerUpType.FREEZE).x(1).y(0).build();
        when(gameSessionManager.checkAndCollectPowerUp(lobbyCode, 1, 0)).thenReturn(p);

        mazeEntity.setLayout("0P\n00");
        mazeEntity.setWidth(2);  
        mazeEntity.setHeight(2);

        GameState resultState = service.movePlayer(gameId, new PlayerMoveRequestDTO(username, "RIGHT"));

        assertNotNull(resultState.getCurrentLayout(), "El layout no debe ser nulo en la respuesta");

        String firstRow = resultState.getCurrentLayout().split("\n")[0].trim();
        assertEquals("00", firstRow, "El GameState retornado debe tener el layout limpio (sin la P)");
    }

    /**
     * Verificar que la respuesta incluye los efectos activos.
     */
    @Test
    void movePlayer_ResponseShouldIncludeActiveEffects() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(gameEntity));
        // Jugador con un efecto activo (CONFUSION)
        PlayerGameStateDTO playerState = new PlayerGameStateDTO();
        playerState.setUsername(username);
        playerState.setPosition(new PositionDTO(1, 1));
        playerState.getActiveEffects().put(PowerUpType.CONFUSION, System.currentTimeMillis() + 10000); 
        
        when(gameSessionManager.getPlayer(lobbyCode, username)).thenReturn(playerState);
        when(gameSessionManager.getPlayers(lobbyCode)).thenReturn(List.of(playerState));

        GameState result = service.movePlayer(gameId, new PlayerMoveRequestDTO(username, "RIGHT"));

        assertNotNull(result.getPlayers(), "La lista de jugadores no debe ser nula");
        assertFalse(result.getPlayers().isEmpty());
        
        PlayerGameStateDTO resultPlayer = result.getPlayers().get(0);
        assertEquals(username, resultPlayer.getUsername());
        
        assertTrue(resultPlayer.getActiveEffects().containsKey(PowerUpType.CONFUSION), 
                   "El cliente debe recibir el mapa con el efecto de confusión activo");
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