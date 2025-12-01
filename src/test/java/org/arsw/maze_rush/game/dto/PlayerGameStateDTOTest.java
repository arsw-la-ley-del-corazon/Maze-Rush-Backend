package org.arsw.maze_rush.game.dto;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;


class PlayerGameStateDTOTest {

    private final String username = "testUserX";
    private final String correctColorTest = "#4ECDC4"; 
    private final String fallbackColor = "#FF6B6B"; 
    private final PositionDTO pos = new PositionDTO(10, 20);

    //  Tests de Constructores y Getters/Setters

    @Test
    void testNoArgsConstructor_DefaultValues() {
        PlayerGameStateDTO dto = new PlayerGameStateDTO();

        assertNull(dto.getUsername());
        assertNull(dto.getPosition());
        assertFalse(dto.getIsFinished()); 
        assertNull(dto.getFinishTime());
        assertNull(dto.getAvatarColor());
    }

    @Test
    void testAllArgsConstructor() {
        PositionDTO testPos = new PositionDTO(3, 4);

        PlayerGameStateDTO dto = new PlayerGameStateDTO(
                "sebastian",
                testPos,
                true,
                120L,
                "#FF0000"
        );

        assertEquals("sebastian", dto.getUsername());
        assertEquals(testPos, dto.getPosition());
        assertTrue(dto.getIsFinished());
        assertEquals(120L, dto.getFinishTime());
        assertEquals("#FF0000", dto.getAvatarColor());
    }

    @Test
    void testCustomConstructor_GeneratesColor() {
        PlayerGameStateDTO dto = new PlayerGameStateDTO(username, pos);

        assertEquals(username, dto.getUsername());
        assertEquals(pos, dto.getPosition());
        assertFalse(dto.getIsFinished());
        assertNotNull(dto.getAvatarColor());
        assertTrue(dto.getAvatarColor().startsWith("#"));
        
        assertEquals(correctColorTest, dto.getAvatarColor(), "El color generado debe ser consistente para el username.");
    }

    @Test
    void testSettersAndGetters() {
        PositionDTO testPos = new PositionDTO(5, 6);
        PlayerGameStateDTO dto = new PlayerGameStateDTO();

        dto.setUsername("testUser");
        dto.setPosition(testPos);
        dto.setIsFinished(true);
        dto.setFinishTime(500L);
        dto.setAvatarColor("#ABCDEF");

        assertEquals("testUser", dto.getUsername());
        assertEquals(testPos, dto.getPosition());
        assertTrue(dto.getIsFinished());
        assertEquals(500L, dto.getFinishTime());
        assertEquals("#ABCDEF", dto.getAvatarColor());
    }

    // Tests de Cobertura de Lógica (generateColorForUsername)

    @Test
    void testGenerateColorForUsername_NoSuchAlgorithmException() {
        try (MockedStatic<MessageDigest> mockedDigest = Mockito.mockStatic(MessageDigest.class)) {
         
            mockedDigest.when(() -> MessageDigest.getInstance(anyString()))
                        .thenThrow(new NoSuchAlgorithmException("Forced exception for coverage"));

            PlayerGameStateDTO dto = new PlayerGameStateDTO(username, pos);

            assertEquals(fallbackColor, dto.getAvatarColor(), "Debe retornar el color por defecto tras la excepción.");

        }
    }
    
    //  Tests de Lombok (@Data: Equals, HashCode, ToString)

    @Test
    void testEqualsAndHashCode_SameObjects() {
        PositionDTO testPos = new PositionDTO(2, 2);

        PlayerGameStateDTO dto1 = new PlayerGameStateDTO("user", testPos, false, 100L, "#123456");
        PlayerGameStateDTO dto2 = new PlayerGameStateDTO("user", testPos, false, 100L, "#123456");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object()); 
    }
    
    @Test
    void testEqualsAndHashCode_DifferentFields() {
        PositionDTO pos1 = new PositionDTO(2, 2);
        PositionDTO pos2 = new PositionDTO(3, 3);
        
        PlayerGameStateDTO dtoBase = new PlayerGameStateDTO("user", pos1, false, 100L, "#123456");

        PlayerGameStateDTO dtoDiffUser = new PlayerGameStateDTO("diff", pos1, false, 100L, "#123456");
        assertNotEquals(dtoBase, dtoDiffUser);

        PlayerGameStateDTO dtoDiffPos = new PlayerGameStateDTO("user", pos2, false, 100L, "#123456");
        assertNotEquals(dtoBase, dtoDiffPos);

        PlayerGameStateDTO dtoDiffFinished = new PlayerGameStateDTO("user", pos1, true, 100L, "#123456");
        assertNotEquals(dtoBase, dtoDiffFinished);

        PlayerGameStateDTO dtoDiffTime = new PlayerGameStateDTO("user", pos1, false, 200L, "#123456");
        assertNotEquals(dtoBase, dtoDiffTime);

        PlayerGameStateDTO dtoDiffColor = new PlayerGameStateDTO("user", pos1, false, 100L, "#ABCDEF");
        assertNotEquals(dtoBase, dtoDiffColor);
    }
    
    @Test
    void testEquals_HandlesNullFields() {
        PositionDTO testPos = new PositionDTO(2, 2);

        PlayerGameStateDTO dtoNull1 = new PlayerGameStateDTO("user", testPos, false, null, null);
        PlayerGameStateDTO dtoNull2 = new PlayerGameStateDTO("user", testPos, false, null, null);
        
        assertEquals(dtoNull1, dtoNull2);
        
        PlayerGameStateDTO dtoNonNull = new PlayerGameStateDTO("user", testPos, false, 100L, null);
        assertNotEquals(dtoNull1, dtoNonNull);
    }

    @Test
    void testToStringNotNull() {
        PlayerGameStateDTO dto = new PlayerGameStateDTO(username, pos);
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains(username));
        assertTrue(s.contains(pos.toString()));
    }
}