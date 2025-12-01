package org.arsw.maze_rush.game.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class GameFinishDTOTest {

    private static final String USERNAME = "speedRunner";
    private static final Long FINISH_TIME = 155L; 
    private static final Instant TEST_TIMESTAMP = Instant.parse("2025-10-26T10:00:00Z");

    //  Tests de Constructores y Getters/Setters
    @Test
    void testNoArgsConstructorAndSetters() {
        GameFinishDTO dto = new GameFinishDTO();

        dto.setUsername(USERNAME);
        dto.setFinishTime(FINISH_TIME);
        dto.setTimestamp(TEST_TIMESTAMP);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(FINISH_TIME, dto.getFinishTime());
        assertEquals(TEST_TIMESTAMP, dto.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        GameFinishDTO dto = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(FINISH_TIME, dto.getFinishTime());
        assertEquals(TEST_TIMESTAMP, dto.getTimestamp());
    }

    @Test
    void testCustomConstructorSetsCurrentTimestamp() {
        Instant before = Instant.now().minus(1, ChronoUnit.SECONDS); 

        GameFinishDTO dto = new GameFinishDTO(USERNAME, FINISH_TIME);
        
        Instant after = Instant.now().plus(1, ChronoUnit.SECONDS); 

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(FINISH_TIME, dto.getFinishTime());
        
        assertNotNull(dto.getTimestamp(), "El timestamp no debe ser nulo");
        
        assertTrue(dto.getTimestamp().isAfter(before), "El timestamp debe ser posterior al inicio del test");
        assertTrue(dto.getTimestamp().isBefore(after), "El timestamp debe ser anterior al final del test");
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCodeSameFields() {
        GameFinishDTO dto1 = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP);
        GameFinishDTO dto2 = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testNotEqualsDifferentFields() {
        GameFinishDTO dtoBase = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP);

        GameFinishDTO dtoDiffUser = new GameFinishDTO("otherUser", FINISH_TIME, TEST_TIMESTAMP);
        assertNotEquals(dtoBase, dtoDiffUser);

        GameFinishDTO dtoDiffFinish = new GameFinishDTO(USERNAME, 100L, TEST_TIMESTAMP);
        assertNotEquals(dtoBase, dtoDiffFinish);

        GameFinishDTO dtoDiffTime = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP.plusSeconds(1));
        assertNotEquals(dtoBase, dtoDiffTime);
    }

    @Test
    void testToStringIsCorrect() {
        GameFinishDTO dto = new GameFinishDTO(USERNAME, FINISH_TIME, TEST_TIMESTAMP);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(USERNAME));
        assertTrue(result.contains(FINISH_TIME.toString()));
        assertTrue(result.contains(TEST_TIMESTAMP.toString()));
    }
}