package org.arsw.maze_rush.game.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameLeaveDTOTest {

    private static final String USERNAME = "playerLeaving";
    private static final String LOBBY_CODE = "XYZ789";

    //  Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructorAndSetters() {
        GameLeaveDTO dto = new GameLeaveDTO();

        dto.setUsername(USERNAME);
        dto.setLobbyCode(LOBBY_CODE);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(LOBBY_CODE, dto.getLobbyCode());
    }

    @Test
    void testAllArgsConstructor() {
        GameLeaveDTO dto = new GameLeaveDTO(USERNAME, LOBBY_CODE);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(LOBBY_CODE, dto.getLobbyCode());
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCodeSameFields() {
        GameLeaveDTO dto1 = new GameLeaveDTO(USERNAME, LOBBY_CODE);
        GameLeaveDTO dto2 = new GameLeaveDTO(USERNAME, LOBBY_CODE);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testNotEqualsDifferentFields() {
        GameLeaveDTO dtoBase = new GameLeaveDTO(USERNAME, LOBBY_CODE);
        
        GameLeaveDTO dtoDiffUser = new GameLeaveDTO("anotherPlayer", LOBBY_CODE);
        assertNotEquals(dtoBase, dtoDiffUser);

        GameLeaveDTO dtoDiffLobby = new GameLeaveDTO(USERNAME, "DIFF000");
        assertNotEquals(dtoBase, dtoDiffLobby);
    }

    @Test
    void testToStringIsCorrect() {
        GameLeaveDTO dto = new GameLeaveDTO(USERNAME, LOBBY_CODE);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(USERNAME));
        assertTrue(result.contains(LOBBY_CODE));
    }
}