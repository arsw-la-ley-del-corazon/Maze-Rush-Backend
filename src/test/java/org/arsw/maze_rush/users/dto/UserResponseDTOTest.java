package org.arsw.maze_rush.users.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserResponseDTOTest {
    
    private static final String ID = "user123";
    private static final String USERNAME = "johndoe";
    private static final String EMAIL = "john@example.com";
    private static final int SCORE = 1500;
    private static final int LEVEL = 5;
    private static final String BIO = "Cazador experto";
    private static final String AVATAR_COLOR = "#AABBCC";
    private static final String MAZE_SIZE = "Grande";
    

    @Test
    void testAllArgsConstructor() {
        UserResponseDTO dto = new UserResponseDTO(
            ID, USERNAME, EMAIL, SCORE, LEVEL, BIO, AVATAR_COLOR, MAZE_SIZE
        );

        assertNotNull(dto, "El objeto DTO no debe ser nulo.");
        assertEquals(ID, dto.getId());
        assertEquals(USERNAME, dto.getUsername());
        assertEquals(EMAIL, dto.getEmail());
        assertEquals(SCORE, dto.getScore());
        assertEquals(LEVEL, dto.getLevel());
        assertEquals(BIO, dto.getBio());
        assertEquals(AVATAR_COLOR, dto.getAvatarColor());
        assertEquals(MAZE_SIZE, dto.getPreferredMazeSize());
    }

    // Setters y Getters
    @Test
    void testSettersAndGetters() {
        UserResponseDTO dto = new UserResponseDTO(); 

        dto.setId(ID);
        dto.setUsername(USERNAME);
        dto.setEmail(EMAIL);
        dto.setScore(SCORE);
        dto.setLevel(LEVEL);
        dto.setBio(BIO);
        dto.setAvatarColor(AVATAR_COLOR);
        dto.setPreferredMazeSize(MAZE_SIZE);

        assertEquals(ID, dto.getId());
        assertEquals(USERNAME, dto.getUsername());
        assertEquals(EMAIL, dto.getEmail());
        assertEquals(SCORE, dto.getScore());
        assertEquals(LEVEL, dto.getLevel());
        assertEquals(BIO, dto.getBio());
        assertEquals(AVATAR_COLOR, dto.getAvatarColor());
        assertEquals(MAZE_SIZE, dto.getPreferredMazeSize());
    }

    // Equals y HashCode
    @Test
    void testEqualsAndHashCode() {
        UserResponseDTO dto1 = new UserResponseDTO(
            "id1", "user", "email@test.com", 10, 1, "bio", "#FFF000", "Mediano"
        );
        UserResponseDTO dto2 = new UserResponseDTO(
            "id1", "user", "email@test.com", 10, 1, "bio", "#FFF000", "Mediano"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals(dto1, dto1); 
        assertNotEquals(null, dto1); 
    }
    
    @Test
    void testNotEqualsDifferentFields() {
        UserResponseDTO dto1 = new UserResponseDTO();
        dto1.setId("x");

        UserResponseDTO dto2 = new UserResponseDTO();
        dto2.setId("y");

        assertNotEquals(dto1, dto2);
        
        assertNotEquals(dto1, new Object()); 
    }

    @Test
    void testToStringIsNotNull() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUsername("tester");

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("tester"));
    }


    @Test
    void testAllFieldsNullAllowed() {
        UserResponseDTO dto = new UserResponseDTO();

        assertNull(dto.getId());
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertEquals(0, dto.getScore()); 
        assertEquals(0, dto.getLevel()); 
        assertNull(dto.getBio());
        assertNull(dto.getAvatarColor());
        assertNull(dto.getPreferredMazeSize());
    }
}