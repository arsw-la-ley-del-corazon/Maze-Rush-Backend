package org.arsw.maze_rush.powerups.service;

import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PowerUpServiceInterfaceTest {

    @Mock
    private PowerUpService powerUpService;

    @Mock
    private MazeEntity mazeEntity;

    @Test
    void testInterfaceContract() {
        List<PowerUp> mockResponse = Collections.emptyList();
        // Configurar el mock para responder a los métodos de la interfaz
        when(powerUpService.generatePowerUps(mazeEntity)).thenReturn(mockResponse);
        when(powerUpService.generatePowerUps(any(), anyList())).thenReturn(mockResponse);

        List<PowerUp> result1 = powerUpService.generatePowerUps(mazeEntity);
        assertNotNull(result1);
        verify(powerUpService).generatePowerUps(mazeEntity);

        List<PowerUp> result2 = powerUpService.generatePowerUps(mazeEntity, Collections.emptyList());
        assertNotNull(result2);
        verify(powerUpService).generatePowerUps(mazeEntity, Collections.emptyList());
    }
}