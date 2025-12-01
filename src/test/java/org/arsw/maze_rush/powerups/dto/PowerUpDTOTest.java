package org.arsw.maze_rush.powerups.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class PowerUpDTOTest {

    //  Cobertura de LOMBOK (@Data: equals, hashCode, toString)

    @Test
    void testLombokGeneratedMethods() {
        //  Instancias
        PowerUpDTO dto1 = new PowerUpDTO("FREEZE", 10, 20, 5);
        PowerUpDTO dto2 = new PowerUpDTO("FREEZE", 10, 20, 5); 
        PowerUpDTO dto3 = new PowerUpDTO("CONFUSION", 5, 5, 10); 

        //  Test Equals
        assertEquals(dto1, dto2, "Deben ser iguales si tienen los mismos datos");
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive, "Debe ser igual a sí mismo (Reflexivo)");
        assertNotEquals(dto1, dto3, "No deben ser iguales si cambian los datos");
        
        // Verificaciones estándar de contrato equals
        assertNotEquals(null, dto1); 
        assertNotEquals(new Object(), dto1);

        //  Test HashCode
        assertEquals(dto1.hashCode(), dto2.hashCode(), "HashCodes deben coincidir");
        assertNotEquals(dto1.hashCode(), dto3.hashCode(), "HashCodes deben diferir");

        //  Test ToString 
        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("PowerUpDTO"));
        assertTrue(stringResult.contains("FREEZE"));
        assertTrue(stringResult.contains("10"));
        assertTrue(stringResult.contains("20"));
        assertTrue(stringResult.contains("5"));
    }

    // Cobertura de Constructor y Getters
    @Test
    void testAllArgsConstructorAndGetters() {
        PowerUpDTO dto = new PowerUpDTO("CLEAR_FOG", 1, 2, 8);

        assertEquals("CLEAR_FOG", dto.getType());
        assertEquals(1, dto.getX());
        assertEquals(2, dto.getY());
        assertEquals(8, dto.getDuration());
    }

    // Cobertura de Setters (@Data los genera)
    @Test
    void testSetters() {
        PowerUpDTO dto = new PowerUpDTO("DUMMY", 0, 0, 0);

        dto.setType("FREEZE");
        dto.setX(50);
        dto.setY(60);
        dto.setDuration(15);

        assertEquals("FREEZE", dto.getType());
        assertEquals(50, dto.getX());
        assertEquals(60, dto.getY());
        assertEquals(15, dto.getDuration());
    }
}