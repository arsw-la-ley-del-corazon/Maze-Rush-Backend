package org.arsw.maze_rush.users.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserStatsDTOTest {

    @Test
    void testLombokGeneratedMethods() {
        //  Instancias
        UserStatsDTO dto1 = UserStatsDTO.builder()
                .username("user1")
                .gamesPlayed(10)
                .build();
        
        UserStatsDTO dto2 = UserStatsDTO.builder()
                .username("user1")
                .gamesPlayed(10)
                .build();

        UserStatsDTO dto3 = new UserStatsDTO("user2", 5, 1, 5000L, 20.0);

        //  Equals & HashCode
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        
        //  ToString
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("user1"));
        
        //  Getters & Setters 
        UserStatsDTO empty = new UserStatsDTO();
        empty.setUsername("test");
        empty.setGamesWon(5);
        
        assertEquals("test", empty.getUsername());
        assertEquals(5, empty.getGamesWon());
    }
}