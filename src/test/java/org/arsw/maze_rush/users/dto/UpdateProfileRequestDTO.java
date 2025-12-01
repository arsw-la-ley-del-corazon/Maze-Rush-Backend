package org.arsw.maze_rush.users.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UpdateProfileRequestDTOTest {

    private static Validator validator;

    private static final int USERNAME_MAX = 50;
    private static final int EMAIL_MAX = 254;
    private static final int BIO_MAX = 200;
    private static final String VALID_HEX_COLOR = "#AABBCC";
    
    private static final String VALID_USERNAME = "newuser";
    private static final String VALID_EMAIL = "update@example.com";
    private static final String VALID_BIO = "Nueva bio";

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    //  Tests de Campos Opcionales y Caso Válido
    @Test
    void testValidEmptyDTO() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Un DTO de actualización con campos null debe ser válido.");
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertNull(dto.getAvatarColor());
    }
    
    @Test
    void testValidFullDTO() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setUsername(VALID_USERNAME);
        dto.setEmail(VALID_EMAIL);
        dto.setBio(VALID_BIO);
        dto.setAvatarColor(VALID_HEX_COLOR);
        dto.setPreferredMazeSize("Pequeño");

        Set<ConstraintViolation<UpdateProfileRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Un DTO con todos los campos válidos debe pasar.");
    }
    
    //  Tests de Validación por Campo
    
    @Test
    void testUsernameSizeViolations() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        
        dto.setUsername("ab"); 
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsMin = validator.validate(dto);
        assertFalse(violationsMin.isEmpty());
        assertTrue(violationsMin.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") && v.getMessage().contains("entre 3 y 50 caracteres")));
        dto.setUsername("a".repeat(USERNAME_MAX + 1));
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsMax = validator.validate(dto);
        assertFalse(violationsMax.isEmpty());
        assertTrue(violationsMax.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") && v.getMessage().contains("entre 3 y 50 caracteres")));
    }

    @Test
    void testEmailViolations() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();

        dto.setEmail("invalid-format@");
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsFormat = validator.validate(dto);
        assertFalse(violationsFormat.isEmpty());
        assertTrue(violationsFormat.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email") && v.getMessage().contains("Formato de email inválido")));
        
        String longEmail = "a".repeat(EMAIL_MAX - 5) + "@b.com";
        dto.setEmail(longEmail);
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsSize = validator.validate(dto);
        assertFalse(violationsSize.isEmpty());
        assertTrue(violationsSize.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email") && v.getMessage().contains("no puede tener más de 254 caracteres")));
    }
    
    @Test
    void testBioSizeViolation() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
    
        dto.setBio("b".repeat(BIO_MAX + 1)); 
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("bio") && v.getMessage().contains("no puede tener más de 200 caracteres")));
    }
    
    @Test
    void testAvatarColorViolations() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        
        dto.setAvatarColor("#XXYYZZ"); 
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsPattern = validator.validate(dto);
        assertFalse(violationsPattern.isEmpty());
        assertTrue(violationsPattern.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("avatarColor") && v.getMessage().contains("formato hex")));
        
        dto.setAvatarColor("#12345"); 
        Set<ConstraintViolation<UpdateProfileRequestDTO>> violationsSize = validator.validate(dto);
        assertFalse(violationsSize.isEmpty());
        assertTrue(violationsSize.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("avatarColor") && v.getMessage().contains("formato hex")));
    }

    //  Tests de Lombok (@Data)

    @Test
    void testEqualsAndHashCode() {
        UpdateProfileRequestDTO dto1 = new UpdateProfileRequestDTO();
        dto1.setUsername(VALID_USERNAME);
        dto1.setAvatarColor(VALID_HEX_COLOR);

        UpdateProfileRequestDTO dto2 = new UpdateProfileRequestDTO();
        dto2.setUsername(VALID_USERNAME);
        dto2.setAvatarColor(VALID_HEX_COLOR);

        // Equivalencia
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Desigualdad
        dto2.setUsername("different");
        assertNotEquals(dto1, dto2);
        
        // Cobertura de Lombok (identidad y canEqual)
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object()); 
    }

    @Test
    void testToStringIsNotNull() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setUsername(VALID_USERNAME);

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains(VALID_USERNAME));
    }
    
    @Test
    void testSettersAndGetters() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        
        dto.setUsername("testuser");
        assertEquals("testuser", dto.getUsername());
        
        dto.setEmail("test@email.com");
        assertEquals("test@email.com", dto.getEmail());
        
        dto.setBio("test bio");
        assertEquals("test bio", dto.getBio());

        dto.setAvatarColor("#112233");
        assertEquals("#112233", dto.getAvatarColor());

        dto.setPreferredMazeSize("Grande");
        assertEquals("Grande", dto.getPreferredMazeSize());
    }
}