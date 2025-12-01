package org.arsw.maze_rush.game.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class GameMoveDTOTest {

    private static final String USERNAME = "movingPlayer";
    private static final Instant testTimeStamp = Instant.parse("2025-10-26T10:00:00Z");
    
    private final PositionDTO testPosition = new PositionDTO(10, 20); 
    private final PositionDTO anotherPosition = new PositionDTO(15, 25);

    // Tests de Constructores y Getters/Setters
    @Test
    void testNoArgsConstructorAndSetters() {
        GameMoveDTO dto = new GameMoveDTO();

        dto.setUsername(USERNAME);
        dto.setPosition(testPosition);
        dto.setTimestamp(testTimeStamp);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(testPosition, dto.getPosition());
        assertEquals(testTimeStamp, dto.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        GameMoveDTO dto = new GameMoveDTO(USERNAME, testPosition, testTimeStamp);

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(testPosition, dto.getPosition());
        assertEquals(testTimeStamp, dto.getTimestamp());
    }

    @Test
    void testCustomConstructorSetsCurrentTimestamp() {
        Instant before = Instant.now().minus(1, ChronoUnit.SECONDS); 

        GameMoveDTO dto = new GameMoveDTO(USERNAME, testPosition);
        
        Instant after = Instant.now().plus(1, ChronoUnit.SECONDS); 

        assertEquals(USERNAME, dto.getUsername());
        assertEquals(testPosition, dto.getPosition());
        
        assertNotNull(dto.getTimestamp(), "El timestamp no debe ser nulo");
        
        assertTrue(dto.getTimestamp().isAfter(before), "El timestamp debe ser posterior al inicio del test");
        assertTrue(dto.getTimestamp().isBefore(after), "El timestamp debe ser anterior al final del test");
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCodeSameFields() {
        GameMoveDTO dto1 = new GameMoveDTO(USERNAME, testPosition, testTimeStamp);
        GameMoveDTO dto2 = new GameMoveDTO(USERNAME, testPosition, testTimeStamp);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testNotEqualsDifferentFields() {
        GameMoveDTO dtoBase = new GameMoveDTO(USERNAME, testPosition, testTimeStamp);
        
        GameMoveDTO dtoDiffUser = new GameMoveDTO("otherUser", testPosition, testTimeStamp);
        assertNotEquals(dtoBase, dtoDiffUser);

        GameMoveDTO dtoDiffPos = new GameMoveDTO(USERNAME, anotherPosition, testTimeStamp);
        assertNotEquals(dtoBase, dtoDiffPos);

        GameMoveDTO dtoDiffTime = new GameMoveDTO(USERNAME, testPosition, testTimeStamp.plusSeconds(1));
        assertNotEquals(dtoBase, dtoDiffTime);
    }

    @Test
    void testToStringIsCorrect() {
        GameMoveDTO dto = new GameMoveDTO(USERNAME, testPosition, testTimeStamp);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(USERNAME));
        assertTrue(result.contains(testPosition.toString())); 
        assertTrue(result.contains(testTimeStamp.toString()));
    }
}