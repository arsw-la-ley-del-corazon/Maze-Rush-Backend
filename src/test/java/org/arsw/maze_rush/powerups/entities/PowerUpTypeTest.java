package org.arsw.maze_rush.powerups.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PowerUpTypeTest {

    @Test
    void testEnumValues() {
        //  Verificar que existen todos los valores definidos
        PowerUpType[] values = PowerUpType.values();
        
        assertEquals(3, values.length, "El enum debe tener exactamente 3 tipos");
        
        //  Verificar que no son nulos
        assertNotNull(PowerUpType.CLEAR_FOG);
        assertNotNull(PowerUpType.FREEZE);
        assertNotNull(PowerUpType.CONFUSION);
    }

    @Test
    void testEnumValueOf() {
        //  Verificar la conversión de String a Enum
        assertEquals(PowerUpType.CLEAR_FOG, PowerUpType.valueOf("CLEAR_FOG"));
        assertEquals(PowerUpType.FREEZE, PowerUpType.valueOf("FREEZE"));
        assertEquals(PowerUpType.CONFUSION, PowerUpType.valueOf("CONFUSION"));
    }
}