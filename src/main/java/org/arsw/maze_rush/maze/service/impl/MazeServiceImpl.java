package org.arsw.maze_rush.maze.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.arsw.maze_rush.common.exceptions.MazeGenerationException;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.repository.MazeRepository;
import org.arsw.maze_rush.maze.service.MazeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

@Service
@Transactional
public class MazeServiceImpl implements MazeService {

    private final MazeRepository mazeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();


    public MazeServiceImpl(MazeRepository mazeRepository) {
        this.mazeRepository = mazeRepository;
    }

    @Override
    public MazeEntity generateMaze(String size) {
        int width;
        int height;

        switch (size.toUpperCase()) {
            case "SMALL" -> width = height = 15;
            case "LARGE" -> width = height = 39; 
            default ->  width = height = 25;  
        }

        int[][] maze = generatePerfectMaze(width, height);

        try {
            String jsonLayout = objectMapper.writeValueAsString(maze);

            MazeEntity mazeEntity = MazeEntity.builder()
                    .size(size.toUpperCase())
                    .width(width)
                    .height(height)
                    .layout(jsonLayout)
                    .startX(1)
                    .startY(1)
                    .goalX(width - 2)
                    .goalY(height - 2)
                    .build();

            return mazeRepository.save(mazeEntity);
        } catch (Exception e) {
            throw new MazeGenerationException("Error generando el laberinto", e);
        }
    }
    // Algoritmo de generación de laberintos: Backtracking
    private int[][] generatePerfectMaze(int width, int height) {
        int[][] maze = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = 1;
            }
        }
        int startX = 1;
        int startY = 1;
        maze[startY][startX] = 0;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});
        int[][] directions = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int cx = current[0];
            int cy = current[1];

            java.util.List<int[]> unvisited = new java.util.ArrayList<>();
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                if (ny > 0 && ny < height - 1 && nx > 0 && nx < width - 1 && maze[ny][nx] == 1) {
                    unvisited.add(new int[]{nx, ny});
                }
            }
            if (unvisited.isEmpty()) {
                stack.pop();
            } else {
                int[] next = unvisited.get(RANDOM.nextInt(unvisited.size()));
                int nx = next[0];
                int ny = next[1];

                maze[(cy + ny) / 2][(cx + nx) / 2] = 0;
                maze[ny][nx] = 0;

                stack.push(next);
            }
        }
        maze[1][1] = 0;
        maze[height - 2][width - 2] = 0;

        return maze;
    }

    @Override
    public MazeEntity getMazeById(UUID id) {
        return mazeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laberinto no encontrado con ID: " + id));
    }
}
