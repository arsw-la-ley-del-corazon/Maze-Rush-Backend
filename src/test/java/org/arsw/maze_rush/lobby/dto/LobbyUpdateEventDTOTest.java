package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LobbyUpdateEventDTOTest {

    private static final String CODE = "RUSH99";
    private static final int PLAYER_COUNT = 5;
    private static final String STATUS = "EN_JUEGO";
    private static final String ACTION = "updated";

    @Test
    void testLombokGeneratedMethods() {
        LobbyUpdateEventDTO dto1 = createFullDTO(CODE, PLAYER_COUNT, STATUS, ACTION);
        LobbyUpdateEventDTO dto2 = createFullDTO(CODE, PLAYER_COUNT, STATUS, ACTION);
        LobbyUpdateEventDTO dto3 = createFullDTO("DIFF", 1, "WAIT", "new");

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("LobbyUpdateEventDTO"));
        assertTrue(s.contains(CODE));
    }

    @Test
    void testNoArgumentConstructor() {
        LobbyUpdateEventDTO dto = new LobbyUpdateEventDTO();
        assertNull(dto.getCode());
        assertEquals(0, dto.getPlayerCount());
    }

    @Test
    void testSettersAndGetters() {
        LobbyUpdateEventDTO dto = new LobbyUpdateEventDTO();
        dto.setCode(CODE);
        dto.setPlayerCount(PLAYER_COUNT);
        dto.setStatus(STATUS);
        dto.setAction(ACTION);

        assertEquals(CODE, dto.getCode());
        assertEquals(PLAYER_COUNT, dto.getPlayerCount());
        assertEquals(STATUS, dto.getStatus());
        assertEquals(ACTION, dto.getAction());
    }

    private LobbyUpdateEventDTO createFullDTO(String code, int playerCount, String status, String action) {
        LobbyUpdateEventDTO dto = new LobbyUpdateEventDTO();
        dto.setCode(code);
        dto.setPlayerCount(playerCount);
        dto.setStatus(status);
        dto.setAction(action);
        return dto;
    }
}