package org.arsw.maze_rush.users.entities;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserStatisticsEntityTest {

    @Test
    void testLombokGeneratedMethods() {
        //  Instancias
        UUID id = UUID.randomUUID();
        UserEntity user = new UserEntity();
        
        UserStatisticsEntity stats1 = new UserStatisticsEntity(id, user, 10, 5, 20, 1000L);
        UserStatisticsEntity stats2 = new UserStatisticsEntity(id, user, 10, 5, 20, 1000L);
        UserStatisticsEntity stats3 = new UserStatisticsEntity(); 

        // Equals
        assertEquals(stats1, stats2);
        boolean isReflexive = stats1.equals(stats1);
        assertTrue(isReflexive, "Debe ser igual a sí mismo (Reflexivo)");
        assertNotEquals(stats1, stats3);
        assertNotEquals(null, stats1);
        assertNotEquals(stats1, new Object());

        //  HashCode
        assertEquals(stats1.hashCode(), stats2.hashCode());
        assertNotEquals(stats1.hashCode(), stats3.hashCode());

        //  ToString
        String s = stats1.toString();
        assertNotNull(s);
        assertTrue(s.contains("UserStatisticsEntity"));
        assertTrue(s.contains("gamesPlayed=10"));
    }

    @Test
    void testBuilderAndNoArgsConstructor() {
        UserStatisticsEntity stats = new UserStatisticsEntity();
        assertNull(stats.getId());
        assertEquals(0, stats.getGamesPlayed()); 
        
        UserStatisticsEntity built = UserStatisticsEntity.builder()
                .gamesPlayed(5)
                .build();
        assertEquals(5, built.getGamesPlayed());
    }

    @Test
    void testUpdateFastestTime() {
        UserStatisticsEntity stats = new UserStatisticsEntity();
        
        // No hay tiempo previo (null) -> Se actualiza
        stats.updateFastestTime(5000L);
        assertEquals(5000L, stats.getFastestTimeMs());

        // Nuevo tiempo es PEOR (mayor) -> NO se actualiza
        stats.updateFastestTime(6000L);
        assertEquals(5000L, stats.getFastestTimeMs());

        // Nuevo tiempo es MEJOR (menor) -> Se actualiza
        stats.updateFastestTime(4000L);
        assertEquals(4000L, stats.getFastestTimeMs());
    }
}