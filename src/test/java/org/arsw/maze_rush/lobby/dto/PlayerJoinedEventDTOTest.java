package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerJoinedEventDTOTest {

    private static final PlayerDTO TEST_PLAYER = new PlayerDTO("p123", "testUser", true, true, 5, 100);
    private static final String ACTION_JOINED = "joined";

    @Test
    void testLombokGeneratedMethods() {
        PlayerJoinedEventDTO dto1 = new PlayerJoinedEventDTO(TEST_PLAYER, ACTION_JOINED);
        PlayerJoinedEventDTO dto2 = new PlayerJoinedEventDTO(TEST_PLAYER, ACTION_JOINED);
        PlayerJoinedEventDTO dto3 = new PlayerJoinedEventDTO(null, "diff");

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("PlayerJoinedEventDTO"));
        assertTrue(s.contains(ACTION_JOINED));
    }

    @Test
    void testNoArgumentConstructor() {
        PlayerJoinedEventDTO dto = new PlayerJoinedEventDTO();
        assertNull(dto.getPlayer());
    }

    @Test
    void testAllArgsConstructor() {
        PlayerJoinedEventDTO dto = new PlayerJoinedEventDTO(TEST_PLAYER, ACTION_JOINED);
        assertEquals(TEST_PLAYER, dto.getPlayer());
        assertEquals(ACTION_JOINED, dto.getAction());
    }

    @Test
    void testSettersAndGetters() {
        PlayerJoinedEventDTO dto = new PlayerJoinedEventDTO();
        dto.setPlayer(TEST_PLAYER);
        dto.setAction(ACTION_JOINED);

        assertEquals(TEST_PLAYER, dto.getPlayer());
        assertEquals(ACTION_JOINED, dto.getAction());
    }
}