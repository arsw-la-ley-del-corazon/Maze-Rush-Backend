package org.arsw.maze_rush.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testLombokMethods() {
        //  Instanciación y Setters
        LoginRequestDTO dto1 = new LoginRequestDTO();
        dto1.setUsername("user1");
        dto1.setEmail("user1@example.com");
        dto1.setPassword("pass123");

        LoginRequestDTO dto2 = new LoginRequestDTO();
        dto2.setUsername("user1");
        dto2.setEmail("user1@example.com");
        dto2.setPassword("pass123");
        
        LoginRequestDTO dto3 = new LoginRequestDTO();
        dto3.setUsername("other");

        //  Getters
        assertEquals("user1", dto1.getUsername());
        assertEquals("user1@example.com", dto1.getEmail());
        assertEquals("pass123", dto1.getPassword());

        //  Equals y HashCode
        assertEquals(dto1, dto2); 
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Mismo objeto -> debe ser igual
        boolean isSame = dto1.equals(dto1);
        assertTrue(isSame);

        // Diferentes valores -> no deben ser iguales
        assertNotEquals(dto1, dto3);
        
        // Comparación con null y otro tipo
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());

        //  ToString 
        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("user1"));
    }

    /**
     * Test para verificar que la validación pasa con datos correctos.
     */
    @Test
    void testValidLoginRequest() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("johndoe");
        dto.setEmail("john.doe@example.com");
        dto.setPassword("securePass");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No debería haber errores de validación");
    }

    /**
     * Test para verificar la restricción @NotBlank en password.
     */
    @Test
    void testInvalidPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("johndoe");
        dto.setPassword("");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    /**
     * Test para verificar la restricción @Email.
     */
    @Test
    void testInvalidEmail() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setPassword("123456");
        dto.setEmail("correo-invalido"); 

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}