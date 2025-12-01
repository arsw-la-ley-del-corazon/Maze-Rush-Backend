package org.arsw.maze_rush.lobby.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LobbyStatusTest {


    @Test
    void testEnumValues() {
        // Obtener todos los valores del enum
        LobbyStatus[] statuses = LobbyStatus.values();

        // Verificar que tenemos la cantidad correcta de estados
        assertEquals(4, statuses.length, "El enum debería tener 4 estados");

        // Verificar que los estados específicos existen y no son nulos
        assertNotNull(LobbyStatus.WAITING);
        assertNotNull(LobbyStatus.STARTING);
        assertNotNull(LobbyStatus.IN_GAME);
        assertNotNull(LobbyStatus.FINISHED);
    }


    @Test
    void testEnumValueOf() {
        // Verificar la conversión de String a Enum
        assertEquals(LobbyStatus.WAITING, LobbyStatus.valueOf("WAITING"));
        assertEquals(LobbyStatus.STARTING, LobbyStatus.valueOf("STARTING"));
        assertEquals(LobbyStatus.IN_GAME, LobbyStatus.valueOf("IN_GAME"));
        assertEquals(LobbyStatus.FINISHED, LobbyStatus.valueOf("FINISHED"));
    }
    
    @Test
    void testInvalidEnumValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            LobbyStatus.valueOf("NON_EXISTENT_STATUS");
        });
    }
}