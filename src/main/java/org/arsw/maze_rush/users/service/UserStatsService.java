package org.arsw.maze_rush.users.service;

import org.arsw.maze_rush.users.dto.UserStatsDTO;

public interface UserStatsService {
    void updateStats(String username, boolean isWinner, long gameDurationMs);
    UserStatsDTO getStats(String username);
}