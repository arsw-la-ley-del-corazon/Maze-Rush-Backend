package org.arsw.maze_rush.powerups.service.impl;

import org.arsw.maze_rush.common.exceptions.InvalidMazeLayoutException;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PowerUpServiceImplTest {

    private PowerUpServiceImpl service;
    private MazeEntity validMaze;

    @BeforeEach
    void setup() {
        service = new PowerUpServiceImpl();

        validMaze = MazeEntity.builder()
                .width(5)
                .height(5)
                .layout("""
                        00000
                        01010
                        00000
                        01010
                        00000
                        """)
                .startX(0)
                .startY(0)
                .goalX(4)
                .goalY(4)
                .build();
    }

    @Test
    void shouldGenerateBetween5And10PowerUps() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        assertTrue(result.size() >= 5 && result.size() <= 10,
                "Debe generar entre 5 y 10 power-ups.");
    }

    @Test
    void generatedPositionsShouldBeInsideMazeBoundaries() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        for (PowerUp p : result) {
            assertTrue(p.getX() >= 0 && p.getX() < validMaze.getWidth());
            assertTrue(p.getY() >= 0 && p.getY() < validMaze.getHeight());
        }
    }

    @Test
    void shouldNotGeneratePowerUpsOnWalls() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        String[] rows = validMaze.getLayout().split("\n");

        for (PowerUp p : result) {
            char tile = rows[p.getY()].charAt(p.getX());
            assertEquals('0', tile, "No debe colocarse sobre paredes");
        }
    }

    @Test
    void shouldNotGenerateDuplicatePositions() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        Set<String> positions = new HashSet<>();

        for (PowerUp p : result) {
            String key = p.getX() + "," + p.getY();
            assertFalse(positions.contains(key), "No puede haber posiciones duplicadas");
            positions.add(key);
        }
    }

    @Test
    void shouldNotUseStartGoalOrPlayerPositions() {
        List<PlayerPosition> players = List.of(
                new PlayerPosition(null, 2, 2, 0)
        );

        List<PowerUp> result = service.generatePowerUps(validMaze, players);

        for (PowerUp p : result) {
            assertNotEquals(0, p.getX() + p.getY(), "No puede estar en la posición inicial (0,0)");
            assertFalse(p.getX() == 4 && p.getY() == 4, "No puede estar en meta");
            assertFalse(p.getX() == 2 && p.getY() == 2, "No puede estar en jugador");
        }
    }

    @Test
    void shouldGenerateValidPowerUpType() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        for (PowerUp p : result) {
            assertNotNull(p.getType());
            assertTrue(Arrays.asList(PowerUpType.values()).contains(p.getType()));
        }
    }

    @Test
    void durationShouldBeBetween5And10() {
        List<PowerUp> result = service.generatePowerUps(validMaze);

        for (PowerUp p : result) {
            assertTrue(p.getDuration() >= 5 && p.getDuration() <= 10);
        }
    }

    @Test
    void emptyLayoutShouldThrowException() {
        MazeEntity invalid = MazeEntity.builder()
                .width(5)
                .height(5)
                .layout("")
                .build();

        assertThrows(InvalidMazeLayoutException.class, () ->
                service.generatePowerUps(invalid)
        );
    }

    @Test
    void invalidRowLengthShouldThrowException() {
        MazeEntity invalid = MazeEntity.builder()
                .width(5)
                .height(5)
                .layout("000\n00000\n00000\n00000\n00000")
                .build();

        assertThrows(InvalidMazeLayoutException.class, () ->
                service.generatePowerUps(invalid)
        );
    }

    @Test
    void invalidCharacterShouldThrowException() {
        MazeEntity invalid = MazeEntity.builder()
                .width(5)
                .height(5)
                .layout("""
                        00A00
                        00000
                        00000
                        00000
                        00000
                        """)
                .build();

        assertThrows(InvalidMazeLayoutException.class, () ->
                service.generatePowerUps(invalid)
        );
    }
}
