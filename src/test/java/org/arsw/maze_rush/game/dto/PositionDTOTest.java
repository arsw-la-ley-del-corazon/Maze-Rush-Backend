package org.arsw.maze_rush.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionDTOTest {

    private final int x1 = 10;
    private final int y1 = 20;
    private final int x2 = 5;
    private final int y2 = 50;

    //  Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructor_DefaultValues() {
        PositionDTO dto = new PositionDTO();

        assertEquals(0, dto.getX());
        assertEquals(0, dto.getY());
    }

    @Test
    void testAllArgsConstructor() {
        PositionDTO dto = new PositionDTO(x1, y1);

        assertEquals(x1, dto.getX());
        assertEquals(y1, dto.getY());
    }

    @Test
    void testSettersAndGetters() {
        PositionDTO dto = new PositionDTO();

        dto.setX(x2);
        dto.setY(y2);

        assertEquals(x2, dto.getX());
        assertEquals(y2, dto.getY());
    }

    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCode_SameValues() {
        PositionDTO dto1 = new PositionDTO(x1, y1);
        PositionDTO dto2 = new PositionDTO(x1, y1);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
      
        assertEquals(dto1, dto1);
    }
    
    @Test
    void testEqualsAndHashCode_DifferentValues() {
        PositionDTO dtoBase = new PositionDTO(x1, y1);

        PositionDTO dtoDiffX = new PositionDTO(x2, y1);
        assertNotEquals(dtoBase, dtoDiffX);

        PositionDTO dtoDiffY = new PositionDTO(x1, y2);
        assertNotEquals(dtoBase, dtoDiffY);
        
        assertNotEquals(null, dtoBase);
        assertNotEquals(dtoBase, new Object()); 
    }

    @Test
    void testToStringIsCorrect() {
        PositionDTO dto = new PositionDTO(x1, y1);
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("x=" + x1));
        assertTrue(s.contains("y=" + y1));
    }
}