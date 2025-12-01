package org.arsw.maze_rush.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Test para cubrir constructores (@NoArgsConstructor, @AllArgsConstructor)
     * y métodos generados por @Data (Getters y Setters).
     */
    @Test
    void testConstructorsAndAccessors() {
        //  Constructor vacío y Setter
        OAuth2LoginRequestDTO dto1 = new OAuth2LoginRequestDTO();
        dto1.setIdToken("token-abc");
        
        assertEquals("token-abc", dto1.getIdToken());

        //  Constructor con argumentos
        OAuth2LoginRequestDTO dto2 = new OAuth2LoginRequestDTO("token-xyz");
        
        assertEquals("token-xyz", dto2.getIdToken());
    }

    /**
     * Test para cubrir equals(), hashCode() y toString().
     * Necesario porque @Data los genera y Sonar revisa su bytecode.
     */
    @Test
    void testEqualsHashCodeAndToString() {
        OAuth2LoginRequestDTO dto1 = new OAuth2LoginRequestDTO("token-123");
        OAuth2LoginRequestDTO dto2 = new OAuth2LoginRequestDTO("token-123"); 
        OAuth2LoginRequestDTO dto3 = new OAuth2LoginRequestDTO("token-999");

        // Test Equals
        assertEquals(dto1, dto2); 
        assertEquals(dto1, dto1); 
        assertNotEquals(dto1, dto3); 
        assertNotEquals(null, dto1);  
        assertNotEquals(dto1, new Object()); 

        // Test HashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        // Test ToString
        String result = dto1.toString();
        assertNotNull(result);
        assertTrue(result.contains("token-123"));
        assertTrue(result.contains("OAuth2LoginRequestDTO"));
    }

    /**
     * Test para verificar que un token válido pasa la validación.
     */
    @Test
    void testValidIdToken() {
        OAuth2LoginRequestDTO dto = new OAuth2LoginRequestDTO("valid-google-token");

        Set<ConstraintViolation<OAuth2LoginRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No debería haber errores con un token válido");
    }

    /**
     * Test para verificar la restricción @NotBlank (null y vacío).
     */
    @Test
    void testInvalidIdToken() {
        // Token es Null
        OAuth2LoginRequestDTO dtoNull = new OAuth2LoginRequestDTO(null);
        Set<ConstraintViolation<OAuth2LoginRequestDTO>> violationsNull = validator.validate(dtoNull);
        
        assertFalse(violationsNull.isEmpty(), "Debe fallar si el token es null");
        assertTrue(violationsNull.stream().anyMatch(v -> v.getMessage().contains("obligatorio")));

        // Token es Vacío
        OAuth2LoginRequestDTO dtoEmpty = new OAuth2LoginRequestDTO("");
        Set<ConstraintViolation<OAuth2LoginRequestDTO>> violationsEmpty = validator.validate(dtoEmpty);
        
        assertFalse(violationsEmpty.isEmpty(), "Debe fallar si el token está vacío");

        // Token es solo espacios (Blank)
        OAuth2LoginRequestDTO dtoBlank = new OAuth2LoginRequestDTO("   ");
        Set<ConstraintViolation<OAuth2LoginRequestDTO>> violationsBlank = validator.validate(dtoBlank);
        
        assertFalse(violationsBlank.isEmpty(), "Debe fallar si el token es solo espacios");
    }
}