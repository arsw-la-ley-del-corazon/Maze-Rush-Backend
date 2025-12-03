package org.arsw.maze_rush.auth.util;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseFactoryTest {

    @Test
    void testBuild_createsValidAuthResponse() {
        UserEntity user = new UserEntity();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("tester");
        user.setEmail("tester@mail.com");
        user.setScore(100);
        user.setLevel(5);

        String access = "ACC-123";
        String refresh = "REF-456";

        long exp = 3600L;

        AuthResponseDTO dto = AuthResponseFactory.build(user, access, refresh, exp);

        assertNotNull(dto);
        assertEquals(access, dto.getAccessToken());
        assertEquals(refresh, dto.getRefreshToken());
        assertEquals(exp, dto.getExpiresIn());

        assertNotNull(dto.getUser());
        assertEquals(id.toString(), dto.getUser().getId());
        assertEquals("tester", dto.getUser().getUsername());
        assertEquals("tester@mail.com", dto.getUser().getEmail());
        assertEquals(100, dto.getUser().getScore());
        assertEquals(5, dto.getUser().getLevel());
    }

    @Test
    void testPrivateConstructor_throwsException() throws Exception {

        var constructor = AuthResponseFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
        );

        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class", ex.getCause().getMessage());
    }

}
