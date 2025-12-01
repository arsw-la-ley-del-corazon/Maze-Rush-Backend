package org.arsw.maze_rush.game.logic.entities;

import org.arsw.maze_rush.users.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPositionTest {

    private final UserEntity mockUser = Mockito.mock(UserEntity.class);
    private final UserEntity mockOtherUser = Mockito.mock(UserEntity.class);
    private final int x = 10;
    private final int y = 20;
    private final int score = 100;

    @Test
    void testNoArgsConstructorAndGettersSetters() {
        PlayerPosition pos = new PlayerPosition();

        pos.setPlayer(mockUser);
        pos.setX(x);
        pos.setY(y);
        pos.setScore(score);

        assertNotNull(pos);
        assertEquals(mockUser, pos.getPlayer());
        assertEquals(x, pos.getX());
        assertEquals(y, pos.getY());
        assertEquals(score, pos.getScore());
    }

    @Test
    void testAllArgsConstructor() {
        PlayerPosition pos = new PlayerPosition(mockUser, x, y, score);

        assertNotNull(pos);
        assertEquals(mockUser, pos.getPlayer());
        assertEquals(x, pos.getX());
        assertEquals(y, pos.getY());
        assertEquals(score, pos.getScore());
    }
    
    @Test
    void testEqualsAndHashCode_Identical() {
        PlayerPosition pos1 = new PlayerPosition(mockUser, x, y, score);
        PlayerPosition pos2 = new PlayerPosition(mockUser, x, y, score);
        
        assertEquals(pos1, pos2, "Objetos idénticos deben ser iguales.");
        assertEquals(pos1.hashCode(), pos2.hashCode(), "Hash codes deben coincidir.");

        boolean isReflexive = pos1.equals(pos1);
        assertTrue(isReflexive, "El objeto debe ser igual a sí mismo.");
    }

    @Test
    void testEquals_Different() {
        PlayerPosition basePos = new PlayerPosition(mockUser, x, y, score);

        PlayerPosition diffPlayer = new PlayerPosition(mockOtherUser, x, y, score);
        PlayerPosition diffX = new PlayerPosition(mockUser, x + 1, y, score);
        PlayerPosition diffScore = new PlayerPosition(mockUser, x, y, score + 1);
        assertNotEquals(basePos, diffPlayer, "Debe ser diferente por jugador.");
        assertNotEquals(basePos, diffX, "Debe ser diferente por posición X.");
        assertNotEquals(basePos, diffScore, "Debe ser diferente por score.");
        assertNotEquals(null, basePos, "No debe ser igual a null.");
        assertNotEquals(new Object(), basePos, "No debe ser igual a otra clase.");
    }

    @Test
    void testToString() {
        PlayerPosition pos = new PlayerPosition(mockUser, x, y, score);
        
        String result = pos.toString();
  
        assertNotNull(result);
        assertTrue(result.contains("x=" + x), "toString debe contener la posición X.");
        assertTrue(result.contains("score=" + score), "toString debe contener el score.");
        assertTrue(result.contains("PlayerPosition"), "toString debe contener el nombre de la clase.");
    }
}