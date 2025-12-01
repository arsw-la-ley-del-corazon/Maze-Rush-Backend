package org.arsw.maze_rush.game.logic.entities;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private final UUID gameID = UUID.randomUUID();
    private final String statusRunning = "RUNNING";
    private final String statusFinished = "FINISHED";
    
 
    @Test
    void testGettersAndSetters() {
        GameState gameState = new GameState();
        List<PlayerPosition> positions = Collections.emptyList();

        gameState.setGameId(gameID);
        gameState.setStatus(statusRunning);
        gameState.setPlayerPositions(positions);

        assertNotNull(gameState);
        assertEquals(gameID, gameState.getGameId());
        assertEquals(statusRunning, gameState.getStatus());
        assertEquals(positions, gameState.getPlayerPositions());
    }

    
    @Test
    void testEqualsAndHashCode_Identical() {

        PlayerPosition pos = new PlayerPosition(null, 1, 1, 0);
        List<PlayerPosition> positions = List.of(pos);

        GameState state1 = new GameState();
        state1.setGameId(gameID);
        state1.setStatus(statusRunning);
        state1.setPlayerPositions(positions);

        GameState state2 = new GameState();
        state2.setGameId(gameID);
        state2.setStatus(statusRunning);
        state2.setPlayerPositions(positions);

        boolean isSame = state1.equals(state2);
        assertTrue(isSame,"Objetos con mismos valores deben ser iguales (equals).");  
        
        assertEquals(state1.hashCode(), state2.hashCode(), "Hash codes deben coincidir.");
    }


    @Test
    void testEquals_Different() {
        GameState state1 = new GameState();
        state1.setGameId(gameID);
        state1.setStatus(statusRunning);

        GameState state2 = new GameState();
        state2.setGameId(UUID.randomUUID()); 
        state2.setStatus(statusFinished);
        
        boolean isSame = state1.equals(state2);
        assertFalse(isSame,"\"Objetos con ID diferente deben ser diferentes.");  
        
        state2.setGameId(gameID); 
        assertNotEquals(state1, state2, "Objetos con status diferente deben ser diferentes.");
        
        assertNotEquals(null, state1, "No debe ser igual a null.");
    }


    @Test
    void testToString() {
        GameState state = new GameState();
        state.setGameId(gameID);

        String result = state.toString();

        assertNotNull(result);
        assertTrue(result.contains(gameID.toString()), "toString debe contener el gameId.");
        assertTrue(result.contains("GameState"), "toString debe contener el nombre de la clase.");
    }
}