package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LobbyCacheDTOTest {

    private static final UUID TEST_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String TEST_CODE = "RUSH01";
    private static final String TEST_MAZE_SIZE = "MEDIUM";
    private static final int TEST_MAX_PLAYERS = 6;
    private static final boolean TEST_IS_PUBLIC = true;
    private static final String TEST_STATUS = "IN_GAME";
    private static final String TEST_CREATOR_USERNAME = "creator1";
    private static final List<String> TEST_PLAYERS = Arrays.asList("playerA", "playerB");

    @Test
    void testLombokGeneratedMethods() {
        LobbyCacheDTO dto1 = createFullDTO(TEST_ID, TEST_CODE);
        LobbyCacheDTO dto2 = createFullDTO(TEST_ID, TEST_CODE);
        LobbyCacheDTO dto3 = createFullDTO(UUID.randomUUID(), "DIFF");

        // Equals
        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());

        // HashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        // ToString
        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("LobbyCacheDTO"));
        assertTrue(s.contains(TEST_CODE));
    }

    @Test
    void testNoArgumentConstructor() {
        LobbyCacheDTO dto = new LobbyCacheDTO();
        assertNull(dto.getId());
        assertEquals(0, dto.getMaxPlayers());
    }

    @Test
    void testSettersAndGetters() {
        LobbyCacheDTO dto = new LobbyCacheDTO();
        dto.setId(TEST_ID);
        dto.setCode(TEST_CODE);
        dto.setMazeSize(TEST_MAZE_SIZE);
        dto.setMaxPlayers(TEST_MAX_PLAYERS);
        dto.setPublic(TEST_IS_PUBLIC);
        dto.setStatus(TEST_STATUS);
        dto.setCreatorUsername(TEST_CREATOR_USERNAME);
        dto.setPlayers(TEST_PLAYERS);

        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_CODE, dto.getCode());
        assertEquals(TEST_MAZE_SIZE, dto.getMazeSize());
        assertEquals(TEST_MAX_PLAYERS, dto.getMaxPlayers());
        assertEquals(TEST_IS_PUBLIC, dto.isPublic());
        assertEquals(TEST_STATUS, dto.getStatus());
        assertEquals(TEST_CREATOR_USERNAME, dto.getCreatorUsername());
        assertEquals(TEST_PLAYERS, dto.getPlayers());
    }

    private LobbyCacheDTO createFullDTO(UUID id, String code) {
        LobbyCacheDTO dto = new LobbyCacheDTO();
        dto.setId(id);
        dto.setCode(code);
        dto.setMazeSize(TEST_MAZE_SIZE);
        dto.setMaxPlayers(TEST_MAX_PLAYERS);
        dto.setPublic(TEST_IS_PUBLIC);
        dto.setStatus(TEST_STATUS);
        dto.setCreatorUsername(TEST_CREATOR_USERNAME);
        dto.setPlayers(TEST_PLAYERS);
        return dto;
    }
}