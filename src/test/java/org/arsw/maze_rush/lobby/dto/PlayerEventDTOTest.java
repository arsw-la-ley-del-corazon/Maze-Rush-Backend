package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerEventDTOTest {

    private static final String TEST_USERNAME = "playerA";
    private static final String TEST_ACTION = "joined";
    private static final List<String> TEST_PLAYERS_LIST = Arrays.asList("playerA", "playerB");
    private static final int TEST_PLAYER_COUNT = 3;
    private static final int TEST_MAX_PLAYERS = 4;

    @Test
    void testLombokGeneratedMethods() {
        PlayerEventDTO dto1 = new PlayerEventDTO(TEST_USERNAME, TEST_ACTION, TEST_PLAYERS_LIST, TEST_PLAYER_COUNT, TEST_MAX_PLAYERS);
        PlayerEventDTO dto2 = new PlayerEventDTO(TEST_USERNAME, TEST_ACTION, TEST_PLAYERS_LIST, TEST_PLAYER_COUNT, TEST_MAX_PLAYERS);
        PlayerEventDTO dto3 = new PlayerEventDTO("diff", "diff", null, 0, 0);

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("PlayerEventDTO"));
        assertTrue(s.contains(TEST_USERNAME));
    }

    @Test
    void testNoArgumentConstructor() {
        PlayerEventDTO dto = new PlayerEventDTO();
        assertNull(dto.getUsername());
    }

    @Test
    void testAllArgsConstructor() {
        PlayerEventDTO dto = new PlayerEventDTO(TEST_USERNAME, TEST_ACTION, TEST_PLAYERS_LIST, TEST_PLAYER_COUNT, TEST_MAX_PLAYERS);
        assertEquals(TEST_USERNAME, dto.getUsername());
    }

    @Test
    void testSettersAndGetters() {
        PlayerEventDTO dto = new PlayerEventDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setAction(TEST_ACTION);
        dto.setPlayers(TEST_PLAYERS_LIST);
        dto.setPlayerCount(TEST_PLAYER_COUNT);
        dto.setMaxPlayers(TEST_MAX_PLAYERS);

        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_ACTION, dto.getAction());
        assertEquals(TEST_PLAYERS_LIST, dto.getPlayers());
        assertEquals(TEST_PLAYER_COUNT, dto.getPlayerCount());
        assertEquals(TEST_MAX_PLAYERS, dto.getMaxPlayers());
    }
}