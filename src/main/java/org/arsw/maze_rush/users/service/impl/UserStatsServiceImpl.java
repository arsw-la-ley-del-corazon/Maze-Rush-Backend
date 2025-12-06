package org.arsw.maze_rush.users.service.impl;

import lombok.RequiredArgsConstructor;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UserStatsDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.entities.UserStatisticsEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.arsw.maze_rush.users.repository.UserStatisticsRepository;
import org.arsw.maze_rush.users.service.UserStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserRepository userRepository;
    private final UserStatisticsRepository statsRepository;

    @Override
    @Transactional
    public void updateStats(String username, boolean isWinner, long gameDurationMs) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));

        UserStatisticsEntity stats = statsRepository.findByUser(user)
                .orElseGet(() -> UserStatisticsEntity.builder().user(user).build());

        stats.setGamesPlayed(stats.getGamesPlayed() + 1);

        if (isWinner) {
            stats.setGamesWon(stats.getGamesWon() + 1);
            if (gameDurationMs > 0) {
                stats.updateFastestTime(gameDurationMs);
            }
        }

        statsRepository.save(stats);
    }

    @Override
    public UserStatsDTO getStats(String username) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        UserStatisticsEntity stats = statsRepository.findByUser(user)
                .orElse(UserStatisticsEntity.builder()
                        .user(user)
                        .gamesPlayed(0)
                        .gamesWon(0)
                        .build());

        double winRate = (stats.getGamesPlayed() > 0)
                ? (double) stats.getGamesWon() / stats.getGamesPlayed() * 100
                : 0.0;

        return UserStatsDTO.builder()
                .username(username)
                .gamesPlayed(stats.getGamesPlayed())
                .gamesWon(stats.getGamesWon())
                .fastestTimeMs(stats.getFastestTimeMs())
                .winRate(winRate)
                .build();
    }
}