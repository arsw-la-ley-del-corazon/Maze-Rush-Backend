package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LobbyWithPlayersResponseDTOTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String CODE = "RUSH01";
    private static final String MAZE_SIZE = "GRANDE";
    private static final int MAX_PLAYERS = 4;
    private static final boolean IS_PUBLIC = true;
    private static final String STATUS = "EN_JUEGO";
    private static final String CREATOR_USERNAME = "creator_user";
    private static final String CREATED_AT = "2025-11-26T14:00:00Z";
    private static final List<String> PLAYERS = Arrays.asList("player1", "player2");

    @Test
    void testLombokGeneratedMethods() {
        LobbyWithPlayersResponseDTO dto1 = createFullDTO(ID, CODE);
        LobbyWithPlayersResponseDTO dto2 = createFullDTO(ID, CODE);
        LobbyWithPlayersResponseDTO dto3 = createFullDTO("diff", "CODE99");

        assertEquals(dto1, dto2);
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("LobbyWithPlayersResponseDTO"));
        assertTrue(s.contains(CODE));
    }

    @Test
    void testNoArgumentConstructor() {
        LobbyWithPlayersResponseDTO dto = new LobbyWithPlayersResponseDTO();
        assertNull(dto.getId());
        assertNull(dto.getPlayers());
    }

    @Test
    void testSettersAndGetters() {
        LobbyWithPlayersResponseDTO dto = new LobbyWithPlayersResponseDTO();
        dto.setId(ID);
        dto.setCode(CODE);
        dto.setMazeSize(MAZE_SIZE);
        dto.setMaxPlayers(MAX_PLAYERS);
        dto.setPublic(IS_PUBLIC);
        dto.setStatus(STATUS);
        dto.setCreatorUsername(CREATOR_USERNAME);
        dto.setCreatedAt(CREATED_AT);
        dto.setPlayers(PLAYERS);

        assertEquals(ID, dto.getId());
        assertEquals(CODE, dto.getCode());
        assertEquals(MAZE_SIZE, dto.getMazeSize());
        assertEquals(MAX_PLAYERS, dto.getMaxPlayers());
        assertEquals(IS_PUBLIC, dto.isPublic());
        assertEquals(STATUS, dto.getStatus());
        assertEquals(CREATOR_USERNAME, dto.getCreatorUsername());
        assertEquals(CREATED_AT, dto.getCreatedAt());
        assertEquals(PLAYERS, dto.getPlayers());
    }

    private LobbyWithPlayersResponseDTO createFullDTO(String id, String code) {
        LobbyWithPlayersResponseDTO dto = new LobbyWithPlayersResponseDTO();
        dto.setId(id);
        dto.setCode(code);
        dto.setMazeSize(MAZE_SIZE);
        dto.setMaxPlayers(MAX_PLAYERS);
        dto.setPublic(IS_PUBLIC);
        dto.setStatus(STATUS);
        dto.setCreatorUsername(CREATOR_USERNAME);
        dto.setCreatedAt(CREATED_AT);
        dto.setPlayers(PLAYERS);
        return dto;
    }
}