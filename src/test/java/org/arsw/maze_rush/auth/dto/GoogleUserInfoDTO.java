package org.arsw.maze_rush.auth.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleUserInfoDTOTest {

    @Test
    void testNoArgsConstructor_DefaultValues() {
        GoogleUserInfoDTO dto = new GoogleUserInfoDTO();

        assertNull(dto.getSub());
        assertNull(dto.getName());
        assertNull(dto.getGivenName());
        assertNull(dto.getFamilyName());
        assertNull(dto.getEmail());
        assertNull(dto.getEmailVerified());
        assertNull(dto.getLocale());
    }

    @Test
    void testAllArgsConstructor() {
        GoogleUserInfoDTO dto = new GoogleUserInfoDTO(
                "12345",
                "Pepito Perez",
                "Pepito",
                "Perez",
                "correo@example.com",
                true,
                "es-CO"
        );

        assertEquals("12345", dto.getSub());
        assertEquals("Pepito Perez", dto.getName());
        assertEquals("Pepito", dto.getGivenName());
        assertEquals("Perez", dto.getFamilyName());
        assertEquals("correo@example.com", dto.getEmail());
        assertTrue(dto.getEmailVerified());
        assertEquals("es-CO", dto.getLocale());
    }

    @Test
    void testSettersAndGetters() {
        GoogleUserInfoDTO dto = new GoogleUserInfoDTO();

        dto.setSub("abc");
        dto.setName("Juan Pérez");
        dto.setGivenName("Juan");
        dto.setFamilyName("Pérez");
        dto.setEmail("juan@example.com");
        dto.setEmailVerified(false);
        dto.setLocale("es");

        assertEquals("abc", dto.getSub());
        assertEquals("Juan Pérez", dto.getName());
        assertEquals("Juan", dto.getGivenName());
        assertEquals("Pérez", dto.getFamilyName());
        assertEquals("juan@example.com", dto.getEmail());
        assertFalse(dto.getEmailVerified());
        assertEquals("es", dto.getLocale());
    }

    @Test
    void testEqualsAndHashCode() {
        GoogleUserInfoDTO dto1 = new GoogleUserInfoDTO(
                "123", "Name", "Given", "Family", "mail@mail.com", true, "es"
        );

        GoogleUserInfoDTO dto2 = new GoogleUserInfoDTO(
                "123", "Name", "Given", "Family", "mail@mail.com", true, "es"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToStringNotNull() {
        GoogleUserInfoDTO dto = new GoogleUserInfoDTO();
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("GoogleUserInfoDTO"));
    }
}
