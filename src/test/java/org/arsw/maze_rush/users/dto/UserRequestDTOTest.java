package org.arsw.maze_rush.users.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserRequestDTOTest {

    private static Validator validator;

    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int EMAIL_MAX_LENGTH = 254;
    private static final int PASSWORD_MAX_LENGTH = 72;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @Test
    void testAllArgsConstructorAndGetters() {
        UserRequestDTO dto = new UserRequestDTO(
            "testuser", 
            "test@example.com", 
            "securepass"
        );

        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("securepass", dto.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        UserRequestDTO dto1 = new UserRequestDTO();
        dto1.setUsername("johndoe");
        dto1.setEmail("john@example.com");
        dto1.setPassword("password123");

        UserRequestDTO dto2 = new UserRequestDTO();
        dto2.setUsername("johndoe");
        dto2.setEmail("john@example.com");
        dto2.setPassword("password123");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object()); 
        
        dto2.setUsername("different");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToStringIsNotNull() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("johndoe"));
        
        assertFalse(dto.toString().contains("password123"), 
            "La contraseña no debería ser visible en el toString por seguridad");
    }


    //  Tests de Validación (Jakarta/Bean Validation)
    @Test
    void testValidUserRequestDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No debería haber errores de validación para un DTO válido");
    }
    
    @Test
    void testBlankAndNullFields() {
        UserRequestDTO dto = new UserRequestDTO();
        
        Set<ConstraintViolation<UserRequestDTO>> violationsNull = validator.validate(dto);
        assertEquals(0, violationsNull.stream().filter(v -> v.getMessage().contains("no debe estar vacío")).count());

        dto.setUsername("");
        dto.setEmail("");
        dto.setPassword("");
        
        Set<ConstraintViolation<UserRequestDTO>> violationsBlank = validator.validate(dto);
        assertFalse(violationsBlank.isEmpty());
        
        Set<String> propertyPaths = violationsBlank.stream()
            .map(v -> v.getPropertyPath().toString())
            .collect(Collectors.toSet());
            
        assertTrue(propertyPaths.contains("username"), "Falta validación de username");
        assertTrue(propertyPaths.contains("email"), "Falta validación de email");
        assertTrue(propertyPaths.contains("password"), "Falta validación de password");
    }

    @Test
    void testInvalidEmailFormat() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("johndoe");
        dto.setEmail("INVALID_EMAIL");
        dto.setPassword("password123");

        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testMinLengthViolations() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("ab");
        dto.setEmail("a@b.com");
        dto.setPassword("1234567");
        
        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
    
    @Test
    void testMaxLengthViolations() {
        UserRequestDTO dto = new UserRequestDTO();

        String longUsername = "a".repeat(USERNAME_MAX_LENGTH + 1);
        String longEmail = "a".repeat(EMAIL_MAX_LENGTH - 4) + "@b.com";
        String longPassword = "p".repeat(PASSWORD_MAX_LENGTH + 1);
        
        dto.setUsername(longUsername);
        dto.setEmail(longEmail);
        dto.setPassword(longPassword);
        
        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);
        assertEquals(4, violations.size(), "Debe haber 4 violaciones por longitud máxima y formato Email."); 

        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")), "Falla username MaxLength");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")), "Falla email MaxLength/Format");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password")), "Falla password MaxLength");
    }
}