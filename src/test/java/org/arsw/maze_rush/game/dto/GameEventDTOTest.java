package org.arsw.maze_rush.game.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class GameEventDTOTest {

    private static final String TYPE_JOIN = "player_joined";
    private static final String USERNAME = "testUser";
    private static final Instant TEST_TIMESTAMP = Instant.parse("2025-10-26T10:00:00Z");


    //  Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructorAndSetters() {

        GameEventDTO dto = new GameEventDTO();

        dto.setType(TYPE_JOIN);
        dto.setUsername(USERNAME);
        dto.setTimestamp(TEST_TIMESTAMP);

        assertEquals(TYPE_JOIN, dto.getType());
        assertEquals(USERNAME, dto.getUsername());
        assertEquals(TEST_TIMESTAMP, dto.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {

        GameEventDTO dto = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP);

        assertEquals(TYPE_JOIN, dto.getType());
        assertEquals(USERNAME, dto.getUsername());
        assertEquals(TEST_TIMESTAMP, dto.getTimestamp());
    }

    @Test
    void testCustomConstructorSetsCurrentTimestamp() {

        Instant before = Instant.now().minus(1, ChronoUnit.SECONDS); 

        GameEventDTO dto = new GameEventDTO(TYPE_JOIN, USERNAME);
 
        Instant after = Instant.now().plus(1, ChronoUnit.SECONDS); 

        assertEquals(TYPE_JOIN, dto.getType());
        assertEquals(USERNAME, dto.getUsername());
        
        assertNotNull(dto.getTimestamp(), "El timestamp no debe ser nulo");

        assertTrue(dto.getTimestamp().isAfter(before), "El timestamp debe ser posterior al inicio del test");
        assertTrue(dto.getTimestamp().isBefore(after), "El timestamp debe ser anterior al final del test");
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCodeSameFields() {
        GameEventDTO dto1 = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP);
        GameEventDTO dto2 = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testNotEqualsDifferentFields() {

        GameEventDTO dtoBase = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP);
        
        GameEventDTO dtoDiffType = new GameEventDTO("player_left", USERNAME, TEST_TIMESTAMP);
        assertNotEquals(dtoBase, dtoDiffType);

        GameEventDTO dtoDiffUser = new GameEventDTO(TYPE_JOIN, "otherUser", TEST_TIMESTAMP);
        assertNotEquals(dtoBase, dtoDiffUser);

        GameEventDTO dtoDiffTime = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP.plusSeconds(1));
        assertNotEquals(dtoBase, dtoDiffTime);
    }

    @Test
    void testToStringIsCorrect() {
        GameEventDTO dto = new GameEventDTO(TYPE_JOIN, USERNAME, TEST_TIMESTAMP);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(TYPE_JOIN));
        assertTrue(result.contains(USERNAME));
        assertTrue(result.contains(TEST_TIMESTAMP.toString()));
    }
}