package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LobbyResponseDTOTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String CODE = "RUSH01";
    private static final String MAZE_SIZE = "GRANDE";
    private static final int MAX_PLAYERS = 4;
    private static final int CURRENT_PLAYERS = 3;
    private static final boolean IS_PUBLIC = true;
    private static final String STATUS = "EN_JUEGO";
    private static final String CREATOR_USERNAME = "creator_user";
    private static final String CREATED_AT = "2025-11-26T14:00:00Z";

    // - TEST PARA  COBERTURA LOMBOK ---
    @Test
    void testLombokGeneratedMethods() {
        LobbyResponseDTO dto1 = createFullDTO(ID, CODE);
        LobbyResponseDTO dto2 = createFullDTO(ID, CODE);
        LobbyResponseDTO dto3 = createFullDTO("diff", "CODE99");

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("LobbyResponseDTO"));
        assertTrue(s.contains(CODE));
    }

    @Test
    void testNoArgumentConstructor() {
        LobbyResponseDTO dto = new LobbyResponseDTO();
        assertNull(dto.getId());
        assertEquals(0, dto.getMaxPlayers());
    }

    @Test
    void testSettersAndGetters() {
        LobbyResponseDTO dto = new LobbyResponseDTO();
        dto.setId(ID);
        dto.setCode(CODE);
        dto.setMazeSize(MAZE_SIZE);
        dto.setMaxPlayers(MAX_PLAYERS);
        dto.setCurrentPlayers(CURRENT_PLAYERS);
        dto.setPublic(IS_PUBLIC);
        dto.setStatus(STATUS);
        dto.setCreatorUsername(CREATOR_USERNAME);
        dto.setCreatedAt(CREATED_AT);

        assertEquals(ID, dto.getId());
        assertEquals(CODE, dto.getCode());
        assertEquals(MAZE_SIZE, dto.getMazeSize());
        assertEquals(MAX_PLAYERS, dto.getMaxPlayers());
        assertEquals(CURRENT_PLAYERS, dto.getCurrentPlayers());
        assertEquals(IS_PUBLIC, dto.isPublic());
        assertEquals(STATUS, dto.getStatus());
        assertEquals(CREATOR_USERNAME, dto.getCreatorUsername());
        assertEquals(CREATED_AT, dto.getCreatedAt());
    }

    private LobbyResponseDTO createFullDTO(String id, String code) {
        LobbyResponseDTO dto = new LobbyResponseDTO();
        dto.setId(id);
        dto.setCode(code);
        dto.setMazeSize(MAZE_SIZE);
        dto.setMaxPlayers(MAX_PLAYERS);
        dto.setCurrentPlayers(CURRENT_PLAYERS);
        dto.setPublic(IS_PUBLIC);
        dto.setStatus(STATUS);
        dto.setCreatorUsername(CREATOR_USERNAME);
        dto.setCreatedAt(CREATED_AT);
        return dto;
    }
}