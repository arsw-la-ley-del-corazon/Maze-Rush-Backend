package org.arsw.maze_rush.lobby.dto;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDTOTest {

    private static final String USERNAME = "testUser";
    private static final String MESSAGE = "Hello World";
    private static final Instant NOW = Instant.now();

    @Test
    void testLombokObjectMethods() {
        ChatMessageDTO dto1 = new ChatMessageDTO(USERNAME, MESSAGE, NOW);
        ChatMessageDTO dto2 = new ChatMessageDTO(USERNAME, MESSAGE, NOW);
        ChatMessageDTO dto3 = new ChatMessageDTO("other", "diff", NOW);  

        String s = dto1.toString();
        assertNotNull(s);
        assertTrue(s.contains("ChatMessageDTO"));
        assertTrue(s.contains(USERNAME));
        assertTrue(s.contains(MESSAGE));

        assertEquals(dto1, dto2, "Deben ser iguales (mismos datos)");
        assertEquals(dto1, dto1, "Debe ser igual a sí mismo (this)");
        assertNotEquals(dto1, dto3, "Debe ser diferente a otro con distintos datos");
        assertNotEquals(null, dto1, "No debe ser igual a null");
        assertNotEquals(dto1, new Object(), "No debe ser igual a otro tipo de objeto");

        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testSettersAndGetters() {
        ChatMessageDTO dto = new ChatMessageDTO();
        
        dto.setUsername("setUser");
        dto.setMessage("setMsg");
        dto.setTimestamp(NOW);

        assertEquals("setUser", dto.getUsername());
        assertEquals("setMsg", dto.getMessage());
        assertEquals(NOW, dto.getTimestamp());
    }


    @Test
    void testNoArgsConstructor() {
        ChatMessageDTO dto = new ChatMessageDTO();
        assertNull(dto.getUsername());
        assertNull(dto.getMessage());
    }


    @Test
    void testAllArgsConstructor() {
        ChatMessageDTO dto = new ChatMessageDTO("u", "m", NOW);
        assertEquals("u", dto.getUsername());
        assertEquals("m", dto.getMessage());
        assertEquals(NOW, dto.getTimestamp());
    }


    @Test
    void testCustomConstructor() {
        Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
        ChatMessageDTO dto = new ChatMessageDTO("customUser", "customMsg");
        Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

        assertEquals("customUser", dto.getUsername());
        assertEquals("customMsg", dto.getMessage());
        assertNotNull(dto.getTimestamp());
     
        assertTrue(dto.getTimestamp().isAfter(before));
        assertTrue(dto.getTimestamp().isBefore(after));
    }
}