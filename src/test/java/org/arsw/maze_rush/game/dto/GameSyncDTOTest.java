package org.arsw.maze_rush.game.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSyncDTOTest {

    private final Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private final List<PlayerGameStateDTO> playerList1 = List.of(
        new PlayerGameStateDTO("p1", new PositionDTO(1, 1))
    );
    private final List<PlayerGameStateDTO> playerList2 = List.of(
        new PlayerGameStateDTO("p2", new PositionDTO(5, 5))
    );

    // Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructor_DefaultValues() {
        GameSyncDTO dto = new GameSyncDTO();

        assertEquals("sync", dto.getType());
        assertNull(dto.getPlayers());
        assertNull(dto.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        GameSyncDTO dto = new GameSyncDTO("custom", playerList1, testTime);

        assertEquals("custom", dto.getType());
        assertEquals(playerList1, dto.getPlayers());
        assertEquals(testTime, dto.getTimestamp());
    }

    @Test
    void testPlayersConstructor_GeneratesTimestamp() {
        Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);

        GameSyncDTO dto = new GameSyncDTO(playerList1);

        Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);
        
        assertEquals("sync", dto.getType());
        assertEquals(playerList1, dto.getPlayers());
        assertNotNull(dto.getTimestamp());
        
        assertTrue(dto.getTimestamp().isAfter(before));
        assertTrue(dto.getTimestamp().isBefore(after));
    }

    @Test
    void testSettersAndGetters() {
        GameSyncDTO dto = new GameSyncDTO();

        dto.setType("sync_update");
        dto.setPlayers(playerList2);
        dto.setTimestamp(testTime);

        assertEquals("sync_update", dto.getType());
        assertEquals(playerList2, dto.getPlayers());
        assertEquals(testTime, dto.getTimestamp());
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCode_SameFields() {
        GameSyncDTO dto1 = new GameSyncDTO("sync", playerList1, testTime);
        GameSyncDTO dto2 = new GameSyncDTO("sync", playerList1, testTime);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object()); 
    }
    
    @Test
    void testNotEqualsDifferentFields() {
        GameSyncDTO dtoBase = new GameSyncDTO("sync", playerList1, testTime);
        
        GameSyncDTO dtoDiffType = new GameSyncDTO("update", playerList1, testTime);
        assertNotEquals(dtoBase, dtoDiffType);

        GameSyncDTO dtoDiffPlayers = new GameSyncDTO("sync", playerList2, testTime);
        assertNotEquals(dtoBase, dtoDiffPlayers);

        GameSyncDTO dtoDiffTime = new GameSyncDTO("sync", playerList1, testTime.plusSeconds(1));
        assertNotEquals(dtoBase, dtoDiffTime);
    }

    @Test
    void testToStringIsCorrect() {
        GameSyncDTO dto = new GameSyncDTO("sync_final", playerList1, testTime);
        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("sync_final"));
        assertTrue(result.contains(testTime.toString()));
        assertTrue(result.contains(playerList1.get(0).getUsername())); 
    }
}