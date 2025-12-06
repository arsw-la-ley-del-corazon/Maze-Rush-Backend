package org.arsw.maze_rush.users.service.impl;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UserStatsDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.entities.UserStatisticsEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.arsw.maze_rush.users.repository.UserStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatisticsRepository statsRepository;

    @InjectMocks
    private UserStatsServiceImpl service;

    // --- Tests para updateStats ---

    @Test
    void updateStats_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
            () -> service.updateStats("ghost", true, 1000L));
        
        verify(statsRepository, never()).save(any());
    }

    @Test
    void updateStats_NewStats_ShouldCreateAndSave() {
        UserEntity user = new UserEntity();
        user.setUsername("player1");
        
        when(userRepository.findByUsernameIgnoreCase("player1")).thenReturn(Optional.of(user));
        when(statsRepository.findByUser(user)).thenReturn(Optional.empty());
        service.updateStats("player1", true, 5000L);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statsRepository).save(captor.capture());
        
        UserStatisticsEntity saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(1, saved.getGamesPlayed());
        assertEquals(1, saved.getGamesWon());
        assertEquals(5000L, saved.getFastestTimeMs());
    }

    @Test
    void updateStats_ExistingStats_ShouldUpdateAccumulatively() {
        UserEntity user = new UserEntity();
        UserStatisticsEntity existingStats = UserStatisticsEntity.builder()
                .user(user)
                .gamesPlayed(10)
                .gamesWon(5)
                .fastestTimeMs(2000L)
                .build();
        
        when(userRepository.findByUsernameIgnoreCase("player1")).thenReturn(Optional.of(user));
        when(statsRepository.findByUser(user)).thenReturn(Optional.of(existingStats));

        service.updateStats("player1", false, 9999L);

        verify(statsRepository).save(existingStats);
        assertEquals(11, existingStats.getGamesPlayed()); 
        assertEquals(5, existingStats.getGamesWon());     
        assertEquals(2000L, existingStats.getFastestTimeMs()); 
    }

    @Test
    void updateStats_NewRecord_ShouldUpdateFastestTime() {
        UserEntity user = new UserEntity();
        UserStatisticsEntity existingStats = UserStatisticsEntity.builder()
                .fastestTimeMs(5000L)
                .build();
        
        when(userRepository.findByUsernameIgnoreCase("player1")).thenReturn(Optional.of(user));
        when(statsRepository.findByUser(user)).thenReturn(Optional.of(existingStats));

        service.updateStats("player1", true, 3000L);

        assertEquals(3000L, existingStats.getFastestTimeMs());
    }

    // --- Tests para getStats ---

    @Test
    void getStats_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getStats("unknown"));
    }

    @Test
    void getStats_NoStatsYet_ShouldReturnDefaults() {
        UserEntity user = new UserEntity();
        when(userRepository.findByUsernameIgnoreCase("newbie")).thenReturn(Optional.of(user));
        when(statsRepository.findByUser(user)).thenReturn(Optional.empty());

        UserStatsDTO result = service.getStats("newbie");

        assertEquals("newbie", result.getUsername());
        assertEquals(0, result.getGamesPlayed());
        assertEquals(0, result.getGamesWon());
        assertEquals(0.0, result.getWinRate());
    }

    @Test
    void getStats_WithStats_ShouldCalculateWinRate() {
        UserEntity user = new UserEntity();
        UserStatisticsEntity stats = UserStatisticsEntity.builder()
                .gamesPlayed(10)
                .gamesWon(8)
                .fastestTimeMs(1234L)
                .build();

        when(userRepository.findByUsernameIgnoreCase("pro")).thenReturn(Optional.of(user));
        when(statsRepository.findByUser(user)).thenReturn(Optional.of(stats));

        UserStatsDTO result = service.getStats("pro");

        assertEquals(10, result.getGamesPlayed());
        assertEquals(8, result.getGamesWon());
        assertEquals(1234L, result.getFastestTimeMs());
        assertEquals(80.0, result.getWinRate()); 
    }
}