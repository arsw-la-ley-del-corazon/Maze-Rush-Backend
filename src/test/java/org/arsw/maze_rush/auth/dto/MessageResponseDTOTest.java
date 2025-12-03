package org.arsw.maze_rush.auth.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageResponseDTOTest {

    /**
     * Prueba los constructores (@NoArgsConstructor y @AllArgsConstructor)
     * y el acceso a los datos (@Data -> Getters/Setters).
     */
    @Test
    void testConstructorsAndAccessors() {
        // 1. Probar Constructor sin argumentos y Setter
        MessageResponseDTO response1 = new MessageResponseDTO();
        response1.setMessage("Mensaje de prueba");
        
        assertEquals("Mensaje de prueba", response1.getMessage());

        // 2. Probar Constructor con todos los argumentos
        MessageResponseDTO response2 = new MessageResponseDTO("Otro mensaje");
        
        assertEquals("Otro mensaje", response2.getMessage());
    }

    /**
     * Prueba los métodos equals() y hashCode() generados por @Data.
     * Es necesario probar igualdad, desigualdad y consistencia.
     */
    @Test
    void testEqualsAndHashCode() {
        MessageResponseDTO dto1 = new MessageResponseDTO("Hola Mundo");
        MessageResponseDTO dto2 = new MessageResponseDTO("Hola Mundo"); // Igual contenido
        MessageResponseDTO dto3 = new MessageResponseDTO("Adiós Mundo"); // Diferente contenido

        // Probar igualdad (mismos valores)
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // Probar identidad (mismo objeto)
        assertEquals(dto1, dto1);

        // Probar desigualdad (diferentes valores)
        assertNotEquals(dto1, dto3);

        // Probar desigualdad con null
        assertNotEquals(null, dto1);

        // Probar desigualdad con otro tipo de objeto
        assertNotEquals(dto1, new Object());
    }

    /**
     * Prueba el método toString() generado por @Data.
     */
    @Test
    void testToString() {
        MessageResponseDTO dto = new MessageResponseDTO("Error interno");
        String stringResult = dto.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("Error interno"));
        assertTrue(stringResult.contains("MessageResponseDTO"));
    }
}