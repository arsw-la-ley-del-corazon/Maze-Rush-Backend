package org.arsw.maze_rush.game.logic.entities;

import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void testGettersAndSetters() {
        GameState gameState = new GameState();
        UUID gameId = UUID.randomUUID();
        String status = "EN_CURSO";
        String layout = "000\n000";
        
        List<PlayerGameStateDTO> players = new ArrayList<>();
        players.add(new PlayerGameStateDTO("user1", new PositionDTO(0,0)));

        gameState.setGameId(gameId);
        gameState.setStatus(status);
        gameState.setCurrentLayout(layout);
        
    
        gameState.setPlayers(players);

        assertEquals(gameId, gameState.getGameId());
        assertEquals(status, gameState.getStatus());
        assertEquals(layout, gameState.getCurrentLayout());
        
        assertEquals(players, gameState.getPlayers());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        List<PlayerGameStateDTO> players = new ArrayList<>();

        GameState state1 = new GameState();
        state1.setGameId(id);
        state1.setPlayers(players); 

        GameState state2 = new GameState();
        state2.setGameId(id);
        state2.setPlayers(players); 
        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());
        
        assertNotNull(state1.toString());
    }
}