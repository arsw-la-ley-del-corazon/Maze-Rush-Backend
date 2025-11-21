package org.arsw.maze_rush.powerups.service.impl;

import org.arsw.maze_rush.common.exceptions.InvalidMazeLayoutException;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PowerUpServiceImpl implements PowerUpService {

    private final Random random = new Random();

    @Override
    public List<PowerUp> generatePowerUps(MazeEntity maze) {

        validateLayoutNotEmpty(maze);

        int count = random.nextInt(6) + 5; 
        List<PowerUp> powerUps = new ArrayList<>();
        Set<String> used = new HashSet<>();

        int w = maze.getWidth();
        int h = maze.getHeight();

        String[][] layout = parseMatrix(maze.getLayout(), w, h);

        while (powerUps.size() < count) {

            int x = random.nextInt(w);
            int y = random.nextInt(h);

            validatePosition(x, y, w, h);

            if (!isFree(layout, x, y)) continue;

            String key = x + "," + y;
            if (used.contains(key)) continue;

            used.add(key);

            powerUps.add(
                    PowerUp.builder()
                            .type(randomType())
                            .duration(randomDuration())
                            .x(x)
                            .y(y)
                            .build()
            );
        }

        return powerUps;
    }

    private void validateLayoutNotEmpty(MazeEntity maze) {
        if (maze.getLayout() == null || maze.getLayout().isBlank()) {
            throw new InvalidMazeLayoutException("El layout del laberinto está vacío o es nulo.");
        }
    }

    private void validatePosition(int x, int y, int w, int h) {
        if (x < 0 || x >= w || y < 0 || y >= h) {
            throw new InvalidMazeLayoutException(
                    "Coordenada inválida generada (" + x + "," + y + ") fuera de límites."
            );
        }
    }

    private boolean isFree(String[][] mat, int x, int y) {
        return mat[y][x].equals("0");
    }

    private PowerUpType randomType() {
        PowerUpType[] values = PowerUpType.values();
        return values[random.nextInt(values.length)];
    }

    private int randomDuration() {
        return random.nextInt(6) + 5; 
    }

    private String[][] parseMatrix(String layout, int width, int height) {

        String[] rows = layout.split("\n");

        if (rows.length != height) {
            throw new InvalidMazeLayoutException(
                    "El número de filas del layout no coincide con la altura declarada del laberinto."
            );
        }

        String[][] matrix = new String[height][width];

        for (int y = 0; y < height; y++) {

            if (rows[y].length() != width) {
                throw new InvalidMazeLayoutException(
                        "Fila " + y + " tiene longitud " + rows[y].length() +
                                " pero se esperaba " + width
                );
            }

            for (int x = 0; x < width; x++) {
                char c = rows[y].charAt(x);

                if (c != '0' && c != '1') {
                    throw new InvalidMazeLayoutException(
                            "Caracter inválido '" + c + "' encontrado en (" + x + "," + y + ")."
                    );
                }

                matrix[y][x] = String.valueOf(c);
            }
        }

        return matrix;
    }
}
