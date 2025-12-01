package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerDTOTest {

    private static final String TEST_ID = "p123";
    private static final String TEST_USERNAME = "player_one";
    private static final Boolean TEST_IS_READY = true;
    private static final Boolean TEST_IS_HOST = false;
    private static final Integer TEST_LEVEL = 10;
    private static final Integer TEST_SCORE = 1500;

    @Test
    void testLombokGeneratedMethods() {
        PlayerDTO dto1 = new PlayerDTO(TEST_ID, TEST_USERNAME, TEST_IS_READY, TEST_IS_HOST, TEST_LEVEL, TEST_SCORE);
        PlayerDTO dto2 = new PlayerDTO(TEST_ID, TEST_USERNAME, TEST_IS_READY, TEST_IS_HOST, TEST_LEVEL, TEST_SCORE);
        PlayerDTO dto3 = new PlayerDTO("diff", "diff", false, true, 1, 0);

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("PlayerDTO"));
        assertTrue(s.contains(TEST_USERNAME));
    }

    @Test
    void testNoArgumentConstructor() {
        PlayerDTO dto = new PlayerDTO();
        assertNull(dto.getId());
    }

    @Test
    void testAllArgsConstructor() {
        PlayerDTO dto = new PlayerDTO(TEST_ID, TEST_USERNAME, TEST_IS_READY, TEST_IS_HOST, TEST_LEVEL, TEST_SCORE);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_IS_READY, dto.getIsReady());
        assertEquals(TEST_IS_HOST, dto.getIsHost());
        assertEquals(TEST_LEVEL, dto.getLevel());
        assertEquals(TEST_SCORE, dto.getScore());
    }

    @Test
    void testSettersAndGetters() {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(TEST_ID);
        dto.setUsername(TEST_USERNAME);
        dto.setIsReady(TEST_IS_READY);
        dto.setIsHost(TEST_IS_HOST);
        dto.setLevel(TEST_LEVEL);
        dto.setScore(TEST_SCORE);

        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_IS_READY, dto.getIsReady());
        assertEquals(TEST_IS_HOST, dto.getIsHost());
        assertEquals(TEST_LEVEL, dto.getLevel());
        assertEquals(TEST_SCORE, dto.getScore());
    }
}