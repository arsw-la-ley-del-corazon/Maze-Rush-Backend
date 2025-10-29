package org.arsw.maze_rush.maze.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.repository.MazeRepository;
import org.arsw.maze_rush.maze.service.MazeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class MazeServiceImpl implements MazeService {

    private final MazeRepository mazeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public MazeServiceImpl(MazeRepository mazeRepository) {
        this.mazeRepository = mazeRepository;
    }

    @Override
    public MazeEntity generateMaze(String size) {
        int width, height;

        switch (size.toUpperCase()) {
            case "SMALL" -> { width = 10; height = 10; }
            case "MEDIUM" -> { width = 20; height = 20; }
            case "LARGE" -> { width = 30; height = 30; }
            default -> throw new IllegalArgumentException("Tamaño de laberinto no válido: " + size);
        }

        int[][] layout = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                layout[y][x] = (random.nextInt(100) < 25) ? 1 : 0; 
            }
        }

        try {
            String jsonLayout = objectMapper.writeValueAsString(layout);

            MazeEntity maze = MazeEntity.builder()
                    .size(size.toUpperCase())
                    .width(width)
                    .height(height)
                    .layout(jsonLayout)
                    .build();

            return mazeRepository.save(maze);
        } catch (Exception e) {
            throw new RuntimeException("Error generando laberinto", e);
        }
    }

    @Override
    public MazeEntity getMazeById(UUID id) {
        return mazeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laberinto no encontrado con ID: " + id));
    }
}
