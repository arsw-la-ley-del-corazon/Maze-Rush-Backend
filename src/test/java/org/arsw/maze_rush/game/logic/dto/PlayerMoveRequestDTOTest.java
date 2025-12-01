package org.arsw.maze_rush.game.logic.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMoveRequestDTOTest {

    private static final String USERNAME = "playerA";
    private static final String DIRECTION = "UP";
    private static final String OTHER_USERNAME = "playerB";
    private static final String OTHER_DIRECTION = "DOWN";

    
    @Test
    void testNoArgsConstructorAndGettersSetters() {
        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();

        dto.setUsername(USERNAME);
        dto.setDirection(DIRECTION);

        assertNotNull(dto, "El DTO no debe ser nulo después del constructor.");
        assertEquals(USERNAME, dto.getUsername(), "El getter de username debe coincidir con el valor seteado.");
        assertEquals(DIRECTION, dto.getDirection(), "El getter de direction debe coincidir con el valor seteado.");
    }

    @Test
    void testEqualsAndHashCode_IdenticalObjects() {
        PlayerMoveRequestDTO dto1 = new PlayerMoveRequestDTO();
        dto1.setUsername(USERNAME);
        dto1.setDirection(DIRECTION);

        PlayerMoveRequestDTO dto2 = new PlayerMoveRequestDTO();
        dto2.setUsername(USERNAME);
        dto2.setDirection(DIRECTION);

        boolean isSame = dto1.equals(dto2);
        assertTrue(isSame,"Objetos con mismos valores deben ser iguales (equals).");  
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Hash codes deben ser iguales para objetos iguales.");
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive, "La igualdad debe ser reflexiva");
    }

    @Test
    void testEqualsAndHashCode_DifferentObjects() {
        PlayerMoveRequestDTO baseDto = new PlayerMoveRequestDTO();
        baseDto.setUsername(USERNAME);
        baseDto.setDirection(DIRECTION);

        PlayerMoveRequestDTO dtoDiffUser = new PlayerMoveRequestDTO();
        dtoDiffUser.setUsername(OTHER_USERNAME); 
        dtoDiffUser.setDirection(DIRECTION);

        PlayerMoveRequestDTO dtoDiffDirection = new PlayerMoveRequestDTO();
        dtoDiffDirection.setUsername(USERNAME);
        dtoDiffDirection.setDirection(OTHER_DIRECTION);
        
        assertNotEquals(null, baseDto, "No debe ser igual a null.");
        assertNotEquals(baseDto, new Object(), "No debe ser igual a otra clase.");
        assertNotEquals(baseDto, dtoDiffUser, "Objetos con diferente username deben ser diferentes.");
        assertNotEquals(baseDto, dtoDiffDirection, "Objetos con diferente dirección deben ser diferentes.");

    }


    @Test
    void testToString() {
        PlayerMoveRequestDTO dto = new PlayerMoveRequestDTO();
        dto.setUsername(USERNAME);
        dto.setDirection(DIRECTION);

        String result = dto.toString();

        assertNotNull(result, "toString no debe ser nulo.");
        assertTrue(result.contains("PlayerMoveRequestDTO"), "toString debe contener el nombre de la clase.");
        assertTrue(result.contains("username=" + USERNAME), "toString debe contener el username.");
        assertTrue(result.contains("direction=" + DIRECTION), "toString debe contener la dirección.");
    }
}