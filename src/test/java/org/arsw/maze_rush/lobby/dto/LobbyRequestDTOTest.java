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


class LobbyRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Test de Lombok
    @Test
    void testLombokGeneratedMethods() {
        //  Instancias
        LobbyRequestDTO dto1 = new LobbyRequestDTO();
        dto1.setMazeSize("Medium");
        dto1.setMaxPlayers(4);
        dto1.setPublic(true);
        dto1.setStatus("WAITING");

        LobbyRequestDTO dto2 = new LobbyRequestDTO();
        dto2.setMazeSize("Medium");
        dto2.setMaxPlayers(4);
        dto2.setPublic(true);
        dto2.setStatus("WAITING");

        LobbyRequestDTO dto3 = new LobbyRequestDTO();
        dto3.setMazeSize("Small"); 

        //  Test Equals
        assertEquals(dto1, dto2, "Deben ser iguales si tienen los mismos valores");
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive,"Debe ser igual a sí mismo");
        assertNotEquals(dto1, dto3, "Deben ser diferentes si cambian los valores");
        assertNotEquals(null, dto1, "No debe ser igual a null");
        assertNotEquals(dto1, new Object(), "No debe ser igual a otro tipo de objeto");

        //  Test HashCode
        assertEquals(dto1.hashCode(), dto2.hashCode(), "HashCodes deben coincidir para objetos iguales");
        assertNotEquals(dto1.hashCode(), dto3.hashCode(), "HashCodes deben diferir para objetos distintos");

        // Test ToString 
        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("LobbyRequestDTO"));
        assertTrue(stringResult.contains("Medium"));
        assertTrue(stringResult.contains("maxPlayers=4"));
        
        //  Test Getters explícitos
        assertEquals("Medium", dto1.getMazeSize());
        assertEquals(4, dto1.getMaxPlayers());
        assertTrue(dto1.isPublic());
        assertEquals("WAITING", dto1.getStatus());
    }


    //  Cobertura de Valores por Defecto

    @Test
    void testDefaultValues() {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        assertEquals("EN_ESPERA", dto.getStatus());
        assertTrue(dto.isPublic());
        assertEquals(0, dto.getMaxPlayers());
    }

    //  Cobertura de VALIDACIONES (Jakarta)

    @Test
    void testValidDTO_ShouldHaveNoViolations() {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize("Mediano");
        dto.setMaxPlayers(4);
        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un DTO válido no debe generar violaciones");
    }

    @ParameterizedTest(name = "maxPlayers inválido: {0}")
    @ValueSource(ints = {1, 0, -1})
    void testMaxPlayers_ShouldFailWhenLessThanMin(int invalidValue) {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize("Mediano");
        dto.setMaxPlayers(invalidValue);
        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("maxPlayers") &&
            v.getMessage().contains("al menos 2") 
        ));
    }

    @ParameterizedTest(name = "maxPlayers inválido: {0}")
    @ValueSource(ints = {5, 6, 10})
    void testMaxPlayers_ShouldFailWhenGreaterThanMax(int invalidValue) {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize("Mediano");
        dto.setMaxPlayers(invalidValue);
        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("maxPlayers") &&
            v.getMessage().contains("máximo") 
        ));
    }

    @ParameterizedTest(name = "mazeSize inválido: \"{0}\"")
    @ValueSource(strings = {"", " "})
    void testMazeSize_ShouldFailWhenBlank(String invalidValue) {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize(invalidValue);
        dto.setMaxPlayers(3);

        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mazeSize")));
    }

    @Test
    void testMazeSize_ShouldFailWhenNull() {
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize(null);
        dto.setMaxPlayers(3);

        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mazeSize")));
    }

    @Test
    void testMazeSize_ShouldFailWhenTooLong() {
        String tooLongString = "A".repeat(21);
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize(tooLongString);
        dto.setMaxPlayers(3);

        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mazeSize")));
    }

    @Test
    void testStatus_ShouldFailWhenTooLong() {
        String tooLongString = "X".repeat(21);
        LobbyRequestDTO dto = new LobbyRequestDTO();
        dto.setMazeSize("Pequeño");
        dto.setMaxPlayers(3);
        dto.setStatus(tooLongString);

        Set<ConstraintViolation<LobbyRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
}