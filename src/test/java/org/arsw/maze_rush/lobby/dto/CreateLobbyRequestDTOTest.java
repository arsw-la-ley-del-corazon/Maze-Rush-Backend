package org.arsw.maze_rush.lobby.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class CreateLobbyRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    //  Cobertura de LOMBOK (@Data: equals, hashCode, toString, getters, setters)
    @Test
    void testLombokGeneratedMethods() {
        CreateLobbyRequestDTO dto1 = new CreateLobbyRequestDTO(4, true);
        CreateLobbyRequestDTO dto2 = new CreateLobbyRequestDTO(4, true);
        CreateLobbyRequestDTO dto3 = new CreateLobbyRequestDTO(8, false); 

        assertEquals(dto1, dto2, "Deben ser iguales si tienen los mismos valores");
        boolean isSame = dto1.equals(dto1);
        assertTrue(isSame,"Debe ser igual a sí mismo");  
        assertNotEquals(dto1, dto3, "Deben ser diferentes si cambian los valores");
        assertNotEquals(null, dto1, "No debe ser igual a null");
        assertNotEquals(dto1, new Object(), "No debe ser igual a otro tipo de objeto");

        assertEquals(dto1.hashCode(), dto2.hashCode(), "HashCodes iguales para objetos iguales");
        assertNotEquals(dto1.hashCode(), dto3.hashCode(), "HashCodes distintos para objetos diferentes");

        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("CreateLobbyRequestDTO"));
        assertTrue(stringResult.contains("maxPlayers=4"));
        assertTrue(stringResult.contains("isPrivate=true"));
        
        CreateLobbyRequestDTO dtoEmpty = new CreateLobbyRequestDTO();
        dtoEmpty.setMaxPlayers(5);
        dtoEmpty.setIsPrivate(false);
        assertEquals(5, dtoEmpty.getMaxPlayers());
        assertFalse(dtoEmpty.getIsPrivate());
    }

    //  Cobertura de CONSTRUCTORES
   
    @Test
    void testNoArgumentConstructor_ShouldSetDefaultValues() {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO();
        assertEquals(4, dto.getMaxPlayers());
        assertTrue(dto.getIsPrivate());
    }

    @Test
    void testAllArgumentConstructor_ShouldSetAllFieldsCorrectly() {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(6, false);
        assertEquals(6, dto.getMaxPlayers());
        assertFalse(dto.getIsPrivate());
    }

    //  Cobertura de VALIDACIONES 
    
    @Test
    void testValidDTO_ShouldHaveNoViolations() {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(4, true);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un DTO válido no debe generar violaciones");
    }
    
    @Test
    void testMaxPlayers_ShouldFailWhenNull() {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(null, true);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("maxPlayers")));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 0, -5})
    void testMaxPlayers_ShouldFailWhenLessThanMin(int invalidValue) {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(invalidValue, true);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        
        assertFalse(violations.isEmpty());
        
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("maxPlayers") 
            && v.getMessage().contains("al menos 2") 
        ));
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 10, 100})
    void testMaxPlayers_ShouldFailWhenGreaterThanMax(int invalidValue) {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(invalidValue, true);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        
        assertFalse(violations.isEmpty());
        
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("maxPlayers") 
            && v.getMessage().contains("máximo es 8") 
        ));
    }
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8})
    void testMaxPlayers_ShouldPassForValidValues(int validValue) {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(validValue, true);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testIsPrivate_ShouldFailWhenNull() {
        CreateLobbyRequestDTO dto = new CreateLobbyRequestDTO(4, null);
        Set<ConstraintViolation<CreateLobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("isPrivate")));
    }
}