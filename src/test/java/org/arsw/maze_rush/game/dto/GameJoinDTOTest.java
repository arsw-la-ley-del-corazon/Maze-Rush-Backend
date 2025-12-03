package org.arsw.maze_rush.game.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameJoinDTOTest {

    private static final String USERNAME = "player1";
    private static final String LOBBY_CODE = "ABCD123";

    //  Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructorAndSetters() {
        GameJoinDTO dto = new GameJoinDTO();

        dto.setUsername(USERNAME);
        dto.setLobbyCode(LOBBY_CODE);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(LOBBY_CODE, dto.getLobbyCode());
    }

    @Test
    void testAllArgsConstructor() {
        GameJoinDTO dto = new GameJoinDTO(USERNAME, LOBBY_CODE);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(LOBBY_CODE, dto.getLobbyCode());
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCodeSameFields() {
        GameJoinDTO dto1 = new GameJoinDTO(USERNAME, LOBBY_CODE);
        GameJoinDTO dto2 = new GameJoinDTO(USERNAME, LOBBY_CODE);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testNotEqualsDifferentFields() {
        GameJoinDTO dtoBase = new GameJoinDTO(USERNAME, LOBBY_CODE);
        
        GameJoinDTO dtoDiffUser = new GameJoinDTO("otherPlayer", LOBBY_CODE);
        assertNotEquals(dtoBase, dtoDiffUser);

        GameJoinDTO dtoDiffLobby = new GameJoinDTO(USERNAME, "ZYX987");
        assertNotEquals(dtoBase, dtoDiffLobby);
    }

    @Test
    void testToStringIsCorrect() {
        GameJoinDTO dto = new GameJoinDTO(USERNAME, LOBBY_CODE);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(USERNAME));
        assertTrue(result.contains(LOBBY_CODE));
    }
}