package org.arsw.maze_rush.game.service;

import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionManagerTest {

    private GameSessionManager sessionManager;
    private static final String LOBBY_CODE = "LOBBY1";
    private static final String USERNAME_1 = "playerA";
    private static final String USERNAME_2 = "playerB";
    private static final String MAZE_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        sessionManager = new GameSessionManager();
    }

    //  Gestión de Jugadores

    @Test
    void testAddPlayer_ShouldInitializePlayerStateAndStartTime() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        assertTrue(sessionManager.sessionExists(LOBBY_CODE), "La sesión debe existir después de añadir un jugador.");
        assertEquals(1, sessionManager.getPlayerCount(LOBBY_CODE), "Debe haber un jugador.");
        assertNotNull(sessionManager.getGameStartTime(LOBBY_CODE), "El tiempo de inicio debe establecerse para el primer jugador.");
        PlayerGameStateDTO player = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1);
        assertNotNull(player, "El jugador debe ser recuperable.");
        assertEquals(USERNAME_1, player.getUsername());
        assertEquals(0, player.getPosition().getX(), "La posición inicial debe ser (0, 0).");
        assertFalse(player.getIsFinished(), "isFinished debe ser falso por defecto.");
    }
    
    @Test
    void testAddPlayer_ShouldNotOverwriteExistingPlayer() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        PlayerGameStateDTO initialPlayer = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1);
        initialPlayer.setPosition(new PositionDTO(10, 10)); 
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        PlayerGameStateDTO finalPlayer = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1);
        assertEquals(1, sessionManager.getPlayerCount(LOBBY_CODE), "El conteo de jugadores no debe cambiar.");
        assertEquals(10, finalPlayer.getPosition().getX(), "El estado inicial no debe ser sobrescrito.");
    }

    @Test
    void testGetPlayers_ShouldReturnAllPlayersInSession() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_2);

        List<PlayerGameStateDTO> players = sessionManager.getPlayers(LOBBY_CODE);

        assertEquals(2, players.size(), "Debe retornar ambos jugadores.");
        assertTrue(players.stream().anyMatch(p -> p.getUsername().equals(USERNAME_1)));
    }
    
    @Test
    void testGetPlayers_ShouldReturnEmptyListForNonExistingSession() {
        List<PlayerGameStateDTO> players = sessionManager.getPlayers("NON_EXISTENT");
        assertTrue(players.isEmpty(), "Debe retornar una lista vacía.");
    }

    // Actualización de Estado
    
    @Test
    void testUpdatePlayerPosition_ShouldUpdatePosition() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        PositionDTO newPos = new PositionDTO(5, 5);
        sessionManager.updatePlayerPosition(LOBBY_CODE, USERNAME_1, newPos);
        PlayerGameStateDTO player = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1);
        assertEquals(5, player.getPosition().getX());
        assertEquals(5, player.getPosition().getY());
    }

    @Test
    void testMarkPlayerFinished_ShouldSetFinishedAndCalculateTime() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.markPlayerFinished(LOBBY_CODE, USERNAME_1);
        PlayerGameStateDTO player = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1);
        assertTrue(player.getIsFinished(), "isFinished debe ser true.");
        assertTrue(player.getFinishTime() >= 0L, "finishTime debe ser calculado (>= 0 segundos).");
    }

    @Test
    void testMarkPlayerFinished_ShouldDoNothingIfAlreadyFinished() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.markPlayerFinished(LOBBY_CODE, USERNAME_1);
        Long firstFinishTime = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1).getFinishTime();
        sessionManager.markPlayerFinished(LOBBY_CODE, USERNAME_1);
        Long secondFinishTime = sessionManager.getPlayer(LOBBY_CODE, USERNAME_1).getFinishTime();

        assertEquals(firstFinishTime, secondFinishTime, "El tiempo de finalización no debe actualizarse si ya terminó.");
    }

    // Eliminación de Sesión
    
    @Test
    void testRemovePlayer_ShouldRemovePlayer() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_2);
        sessionManager.removePlayer(LOBBY_CODE, USERNAME_1);
        assertEquals(1, sessionManager.getPlayerCount(LOBBY_CODE), "Debe quedar un jugador.");
        assertNull(sessionManager.getPlayer(LOBBY_CODE, USERNAME_1), "El jugador removido debe ser nulo.");
        assertNotNull(sessionManager.getPlayer(LOBBY_CODE, USERNAME_2), "El otro jugador debe permanecer.");
        assertTrue(sessionManager.sessionExists(LOBBY_CODE), "La sesión no debe eliminarse si quedan jugadores.");
    }

    @Test
    void testRemovePlayer_ShouldClearSessionIfLastPlayer() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.removePlayer(LOBBY_CODE, USERNAME_1);
        assertFalse(sessionManager.sessionExists(LOBBY_CODE), "La sesión debe eliminarse si el último jugador se va.");
        assertNull(sessionManager.getGameStartTime(LOBBY_CODE), "El tiempo de inicio también debe eliminarse.");
    }

    @Test
    void testClearSession_ShouldRemoveAllData() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.setMaze(LOBBY_CODE, MAZE_ID);
        sessionManager.clearSession(LOBBY_CODE);
        assertFalse(sessionManager.sessionExists(LOBBY_CODE), "La sesión debe ser eliminada.");
        assertNull(sessionManager.getGameStartTime(LOBBY_CODE), "El tiempo de inicio debe ser nulo.");
        assertNull(sessionManager.getMazeId(LOBBY_CODE), "El ID del laberinto debe ser nulo.");
    }

    // Métodos de Lectura y Utilidad
    
    @Test
    void testGetActiveSessions_ShouldReturnCorrectCodes() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        sessionManager.addPlayer("LOBBY_2", USERNAME_1);
        var activeSessions = sessionManager.getActiveSessions();
        assertEquals(2, activeSessions.size());
        assertTrue(activeSessions.contains(LOBBY_CODE));
        assertTrue(activeSessions.contains("LOBBY_2"));
    }

    @Test
    void testGetElapsedTime_ShouldCalculateTimeDifference() {
        sessionManager.addPlayer(LOBBY_CODE, USERNAME_1);
        Long elapsed = sessionManager.getElapsedTime(LOBBY_CODE);
        assertTrue(elapsed >= 0L, "El tiempo transcurrido debe ser mayor o igual a 0.");
    }
    
    @Test
    void testGetElapsedTime_ShouldReturnZeroForNonExistingSession() {
        Long elapsed = sessionManager.getElapsedTime("NON_EXISTENT");
        assertEquals(0L, elapsed, "Debe retornar 0L si el tiempo de inicio no existe.");
    }
    
    @Test
    void testMazeManagement_ShouldSetAndGetMazeId() {
        sessionManager.setMaze(LOBBY_CODE, MAZE_ID);

        assertTrue(sessionManager.hasMaze(LOBBY_CODE), "hasMaze debe ser true después de setMaze.");
        assertEquals(MAZE_ID, sessionManager.getMazeId(LOBBY_CODE), "El ID del laberinto debe ser recuperado correctamente.");
    }
    
    @Test
    void testMazeManagement_ShouldReturnNullForNonExistingMaze() {
        assertFalse(sessionManager.hasMaze("OTHER_LOBBY"));
        assertNull(sessionManager.getMazeId("OTHER_LOBBY"));
    }
}