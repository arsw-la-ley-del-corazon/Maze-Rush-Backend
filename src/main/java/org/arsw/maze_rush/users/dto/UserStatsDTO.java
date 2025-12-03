package org.arsw.maze_rush.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private String username;
    private int gamesPlayed;
    private int gamesWon;
    private Long fastestTimeMs;
    private double winRate;
}