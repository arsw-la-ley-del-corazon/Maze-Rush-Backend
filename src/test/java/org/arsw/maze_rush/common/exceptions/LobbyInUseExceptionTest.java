package org.arsw.maze_rush.common.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LobbyInUseExceptionTest {

    @Test
    void testExceptionStoresMessageCorrectly() {
        String message = "El lobby ya está en uso";

        LobbyInUseException ex = new LobbyInUseException(message);

        assertEquals(message, ex.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        LobbyInUseException ex = new LobbyInUseException("mensaje");

        assertTrue(ex instanceof RuntimeException);
    }
}
