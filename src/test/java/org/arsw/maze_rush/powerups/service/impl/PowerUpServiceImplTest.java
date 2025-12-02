package org.arsw.maze_rush.powerups.service.impl;

import org.arsw.maze_rush.common.exceptions.InvalidMazeLayoutException;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PowerUpServiceImplTest {

    @InjectMocks
    private PowerUpServiceImpl powerUpService;

    private MazeEntity validMaze;

    @BeforeEach
    void setUp() {
        validMaze = new MazeEntity();
        validMaze.setWidth(5);
        validMaze.setHeight(5);
        String layout = """
            00000
            00000
            00000
            00000
            00000""";
        validMaze.setLayout(layout);
        validMaze.setStartX(0); // (0,0) ocupado por inicio
        validMaze.setStartY(0);
        validMaze.setGoalX(4);  // (4,4) ocupado por meta
        validMaze.setGoalY(4);
    }

    // TEST: Verifica que la "Bolsa" funcione y respete los límites exactos.
     
    @Test
    void testGeneratePowerUps_CheckExactQuantities() {
        List<PowerUp> powerUps = powerUpService.generatePowerUps(validMaze);

        assertNotNull(powerUps);
        assertEquals(4, powerUps.size(), "Debe generar exactamente 4 powerups según la configuración.");
        Map<PowerUpType, Long> counts = powerUps.stream()
                .collect(Collectors.groupingBy(PowerUp::getType, Collectors.counting()));

        assertEquals(1, counts.getOrDefault(PowerUpType.CLEAR_FOG, 0L), "Debe haber 1 CLEAR_FOG");
        assertEquals(2, counts.getOrDefault(PowerUpType.FREEZE, 0L), "Debe haber 2 FREEZE");
        assertEquals(1, counts.getOrDefault(PowerUpType.CONFUSION, 0L), "Debe haber 1 CONFUSION");
    }

    /**
     * Verifica que los powerups tengan una duración válida y coordenadas dentro del mapa.
     */
    @Test
    void testGeneratePowerUps_AttributesValidity() {
        List<PowerUp> powerUps = powerUpService.generatePowerUps(validMaze);

        for (PowerUp p : powerUps) {
            // Coordenadas válidas
            assertTrue(p.getX() >= 0 && p.getX() < 5);
            assertTrue(p.getY() >= 0 && p.getY() < 5);
            
            // Duración aleatoria 
            assertTrue(p.getDuration() >= 5 && p.getDuration() <= 10);
            
            // No deben estar en Inicio (0,0) ni Fin (4,4)
            assertFalse(p.getX() == 0 && p.getY() == 0);
            assertFalse(p.getX() == 4 && p.getY() == 4);
        }
    }

    /**
     * Verifica que NO se generen powerups donde hay jugadores.
     */
    @Test
    void testGeneratePowerUps_AvoidPlayers() {
        PlayerPosition player = new PlayerPosition();
        player.setX(2);
        player.setY(2);
        List<PlayerPosition> players = Collections.singletonList(player);

        List<PowerUp> powerUps = powerUpService.generatePowerUps(validMaze, players);

        // Ningún powerup debe tener coordenadas (2,2)
        boolean collision = powerUps.stream()
                .anyMatch(p -> p.getX() == 2 && p.getY() == 2);
        
        assertFalse(collision, "Se generó un powerup encima de un jugador");
    }

    /**
     * TEST: Laberinto sin espacio.
     */
    @Test
    void testGeneratePowerUps_NoSpaceAvailable() {
        String wallMaze = """
                11111
                11111
                11111
                11111 
                11111;

                """;
            
        validMaze.setLayout(wallMaze);
        
        List<PowerUp> powerUps = powerUpService.generatePowerUps(validMaze);

        assertTrue(powerUps.isEmpty(), "No debería generar powerups si no hay espacio libre");
    }

    // --- Tests de Validaciones de Layout 
    @Test
    void testValidateLayout_NullOrEmpty() {
        validMaze.setLayout(null);
        assertThrows(InvalidMazeLayoutException.class, () -> powerUpService.generatePowerUps(validMaze));
        
        validMaze.setLayout("");
        assertThrows(InvalidMazeLayoutException.class, () -> powerUpService.generatePowerUps(validMaze));
    }

    @Test
    void testParseMatrix_InvalidDimensions() {
        validMaze.setLayout("00000\n00000");
        assertThrows(InvalidMazeLayoutException.class, () -> 
            powerUpService.generatePowerUps(validMaze)
        );
    }

    @Test
    void testParseMatrix_InvalidCharacter() {
        validMaze.setLayout("00X00\n00000\n00000\n00000\n00000");
        
        List<PowerUp> result = powerUpService.generatePowerUps(validMaze);
        assertNotNull(result);
        boolean onInvalidChar = result.stream().anyMatch(p -> p.getX() == 2 && p.getY() == 0);
        assertFalse(onInvalidChar);
    }
}