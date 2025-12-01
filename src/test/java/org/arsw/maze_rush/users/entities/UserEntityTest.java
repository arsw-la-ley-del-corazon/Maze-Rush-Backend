package org.arsw.maze_rush.users.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void testDefaultValues() {
        UserEntity user = new UserEntity();

        assertEquals(0, user.getScore());
        assertEquals(1, user.getLevel());
        assertEquals("#A46AFF", user.getAvatarColor());
        assertEquals("Mediano", user.getPreferredMazeSize());
        assertEquals(AuthProvider.LOCAL, user.getAuthProvider());
    }

    @Test
    void testSettersAndGetters() {
        UserEntity user = new UserEntity();

        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        user.setScore(10);
        user.setLevel(3);
        user.setBio("bio text");
        user.setAvatarColor("#123456");
        user.setPreferredMazeSize("Grande");
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId("google123");

        assertEquals(id, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(10, user.getScore());
        assertEquals(3, user.getLevel());
        assertEquals("bio text", user.getBio());
        assertEquals("#123456", user.getAvatarColor());
        assertEquals("Grande", user.getPreferredMazeSize());
        assertEquals(AuthProvider.GOOGLE, user.getAuthProvider());
        assertEquals("google123", user.getProviderId());
    }

    @Test
    void testPrePersistGeneratesIdAndTimestamps() {
        UserEntity user = new UserEntity();
        assertNull(user.getId());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        user.onCreate();

        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void testPrePersistKeepsExistingId() {
        UserEntity user = new UserEntity();
        UUID existing = UUID.randomUUID();
        user.setId(existing);

        user.onCreate();

        assertEquals(existing, user.getId());
    }

    @Test
    void testPreUpdateChangesUpdatedAt() {
        UserEntity user = new UserEntity();
        user.onCreate();
        Instant oldDate = Instant.now().minusSeconds(60); 
        user.setUpdatedAt(oldDate);
        user.onUpdate();
        Instant newDate = user.getUpdatedAt();
        assertNotEquals(oldDate, newDate, "onUpdate debería actualizar la fecha de modificación");
        assertTrue(newDate.isAfter(oldDate), "La nueva fecha debe ser posterior a la antigua");
    }

    @Test
    void testToStringDoesNotExposePassword() {
        UserEntity user = new UserEntity();
        user.setUsername("john");
        user.setPassword("mypassword");

        String text = user.toString();

        assertTrue(text.contains("john"));
        assertFalse(text.contains("mypassword"));
    }
}
