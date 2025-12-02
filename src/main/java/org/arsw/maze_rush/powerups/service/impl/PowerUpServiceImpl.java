package org.arsw.maze_rush.powerups.service.impl;

import org.arsw.maze_rush.common.exceptions.InvalidMazeLayoutException;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class PowerUpServiceImpl implements PowerUpService {

    private final SecureRandom random = new SecureRandom();

    private static final Map<PowerUpType, Integer> POWER_UP_LIMITS = Map.of(
        PowerUpType.CLEAR_FOG, 1,
        PowerUpType.FREEZE, 2,   
        PowerUpType.CONFUSION, 1 
    );

    @Override
    public List<PowerUp> generatePowerUps(MazeEntity maze) {
        return generatePowerUps(maze, null);
    }

    @Override
    public List<PowerUp> generatePowerUps(MazeEntity maze, List<PlayerPosition> players) {
        validateLayoutNotEmpty(maze);

        List<PowerUpType> typeBag = createTypeBag();
        
        Collections.shuffle(typeBag, random);

        List<PowerUp> powerUps = new ArrayList<>();
        Set<String> usedPositions = new HashSet<>();

        int w = maze.getWidth();
        int h = maze.getHeight();
        String[][] layout = parseMatrix(maze.getLayout(), w, h);

        usedPositions.add(maze.getStartX() + "," + maze.getStartY());
        usedPositions.add(maze.getGoalX() + "," + maze.getGoalY());
        if (players != null) {
            for (PlayerPosition p : players) {
                usedPositions.add(p.getX() + "," + p.getY());
            }
        }

        while (!typeBag.isEmpty()) {
            int attempts = 0;
            boolean placed = false;

            while (attempts < 20 && !placed) {
                int x = random.nextInt(w);
                int y = random.nextInt(h);
                validatePosition(x, y, w, h);

                String key = x + "," + y;
                boolean isWall = !isFree(layout, x, y);
                boolean isOccupied = usedPositions.contains(key);

                if (!isWall && !isOccupied) {
                    PowerUpType typeToPlace = typeBag.remove(0); 
                    
                    usedPositions.add(key);
                    powerUps.add(
                        PowerUp.builder()
                            .type(typeToPlace)
                            .duration(randomDuration())
                            .x(x)
                            .y(y)
                            .build()
                    );
                    placed = true;
                }
                attempts++;
            }
            
            if (!placed) break; 
        }

        return powerUps;
    }


    private List<PowerUpType> createTypeBag() {
        List<PowerUpType> bag = new ArrayList<>();
        POWER_UP_LIMITS.forEach((type, count) -> {
            for (int i = 0; i < count; i++) {
                bag.add(type);
            }
        });
        return bag;
    }

    private void validateLayoutNotEmpty(MazeEntity maze) {
        if (maze.getLayout() == null || maze.getLayout().isBlank()) {
            throw new InvalidMazeLayoutException("El layout del laberinto está vacío o es nulo.");
        }
    }

    private void validatePosition(int x, int y, int w, int h) {
        if (x < 0 || x >= w || y < 0 || y >= h) {
            throw new InvalidMazeLayoutException("Coordenada fuera de límites.");
        }
    }

    private boolean isFree(String[][] mat, int x, int y) {
        return "0".equals(mat[y][x]);
    }

    private int randomDuration() {
        return random.nextInt(6) + 5; 
    }

    private String[][] parseMatrix(String layout, int width, int height) {
        String[] rows = layout.split("\n");
        if (rows.length != height) {
            throw new InvalidMazeLayoutException(
                "El layout tiene " + rows.length + " filas, pero se esperaban " + height
            );
        }
        
        String[][] matrix = new String[height][width];
        for (int y = 0; y < height; y++) {
            String row = rows[y].trim();
            for (int x = 0; x < width; x++) {
                if (x < row.length()) {
                    matrix[y][x] = String.valueOf(row.charAt(x));
                } else {
                    matrix[y][x] = "1";
                }
            }
        }
        return matrix;
    }
}