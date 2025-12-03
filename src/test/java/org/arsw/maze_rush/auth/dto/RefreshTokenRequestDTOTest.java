package org.arsw.maze_rush.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testLombokGeneratedMethods() {
        //  Instanciación y Setters
        RefreshTokenRequestDTO dto1 = new RefreshTokenRequestDTO();
        dto1.setRefreshToken("token-A");

        RefreshTokenRequestDTO dto2 = new RefreshTokenRequestDTO();
        dto2.setRefreshToken("token-A"); 

        RefreshTokenRequestDTO dto3 = new RefreshTokenRequestDTO();
        dto3.setRefreshToken("token-B"); 

        //  Getters
        assertEquals("token-A", dto1.getRefreshToken());

        // Equals
        assertEquals(dto1, dto2);     
        boolean isSame = dto1.equals(dto1);
        assertTrue(isSame);    
        assertNotEquals(dto1, dto3);
        assertNotEquals(null, dto1);  
        assertNotEquals(dto1, new Object()); 

        //  HashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        //  ToString
        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("token-A"));
        assertTrue(stringResult.contains("RefreshTokenRequestDTO"));
    }

    /**
     * Prueba funcional para asegurar que @NotBlank funciona correctamente.
     */
    @Test
    void testValidation() {
        // Token válido
        RefreshTokenRequestDTO validDto = new RefreshTokenRequestDTO();
        validDto.setRefreshToken("valid-token-123");
        Set<ConstraintViolation<RefreshTokenRequestDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "No debe haber errores con un token válido");

        // Token Null
        RefreshTokenRequestDTO nullTokenDto = new RefreshTokenRequestDTO();
        Set<ConstraintViolation<RefreshTokenRequestDTO>> violationsNull = validator.validate(nullTokenDto);
        assertFalse(violationsNull.isEmpty(), "Debe fallar si el token es null");
        assertTrue(violationsNull.stream().anyMatch(v -> v.getMessage().contains("obligatorio")));

        // Token Vacío
        RefreshTokenRequestDTO emptyTokenDto = new RefreshTokenRequestDTO();
        emptyTokenDto.setRefreshToken("");
        Set<ConstraintViolation<RefreshTokenRequestDTO>> violationsEmpty = validator.validate(emptyTokenDto);
        assertFalse(violationsEmpty.isEmpty(), "Debe fallar si el token está vacío");
        
        // Token con espacios en blanco (Blank)
        RefreshTokenRequestDTO blankTokenDto = new RefreshTokenRequestDTO();
        blankTokenDto.setRefreshToken("   ");
        Set<ConstraintViolation<RefreshTokenRequestDTO>> violationsBlank = validator.validate(blankTokenDto);
        assertFalse(violationsBlank.isEmpty(), "Debe fallar si el token es solo espacios");
    }
}