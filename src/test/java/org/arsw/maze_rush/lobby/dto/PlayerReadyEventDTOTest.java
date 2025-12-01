package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerReadyEventDTOTest {

    private static final String TEST_PLAYER_ID = "p987";
    private static final String TEST_USERNAME = "player_ready_user";
    private static final Boolean TEST_IS_READY = true;

    @Test
    void testLombokGeneratedMethods() {
        PlayerReadyEventDTO dto1 = new PlayerReadyEventDTO(TEST_PLAYER_ID, TEST_USERNAME, TEST_IS_READY);
        PlayerReadyEventDTO dto2 = new PlayerReadyEventDTO(TEST_PLAYER_ID, TEST_USERNAME, TEST_IS_READY);
        PlayerReadyEventDTO dto3 = new PlayerReadyEventDTO("diff", "diff", false);

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("PlayerReadyEventDTO"));
        assertTrue(s.contains(TEST_USERNAME));
    }

    @Test
    void testNoArgumentConstructor() {
        PlayerReadyEventDTO dto = new PlayerReadyEventDTO();
        assertNull(dto.getPlayerId());
    }

    @Test
    void testAllArgsConstructor() {
        PlayerReadyEventDTO dto = new PlayerReadyEventDTO(TEST_PLAYER_ID, TEST_USERNAME, TEST_IS_READY);
        assertEquals(TEST_PLAYER_ID, dto.getPlayerId());
        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_IS_READY, dto.getIsReady());
    }

    @Test
    void testSettersAndGetters() {
        PlayerReadyEventDTO dto = new PlayerReadyEventDTO();
        dto.setPlayerId(TEST_PLAYER_ID);
        dto.setUsername(TEST_USERNAME);
        dto.setIsReady(TEST_IS_READY);

        assertEquals(TEST_PLAYER_ID, dto.getPlayerId());
        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_IS_READY, dto.getIsReady());
    }
}