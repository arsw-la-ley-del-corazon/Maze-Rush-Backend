package org.arsw.maze_rush.maze.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MazeEntityTest {

    @Test
    void testNoArgsConstructor_DefaultValues() {
        MazeEntity maze = new MazeEntity();

        assertNull(maze.getId());
        assertNull(maze.getSize());
        assertEquals(0, maze.getWidth());
        assertEquals(0, maze.getHeight());
        assertNull(maze.getLayout());
        assertEquals(0, maze.getStartX());
        assertEquals(0, maze.getStartY());
        assertEquals(0, maze.getGoalX());
        assertEquals(0, maze.getGoalY());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();

        MazeEntity maze = new MazeEntity(
                id,
                "10x10",
                10,
                10,
                "########\n#......#\n########",
                1,
                1,
                8,
                8
        );

        assertEquals(id, maze.getId());
        assertEquals("10x10", maze.getSize());
        assertEquals(10, maze.getWidth());
        assertEquals(10, maze.getHeight());
        assertEquals("########\n#......#\n########", maze.getLayout());
        assertEquals(1, maze.getStartX());
        assertEquals(1, maze.getStartY());
        assertEquals(8, maze.getGoalX());
        assertEquals(8, maze.getGoalY());
    }

    @Test
    void testBuilder() {
        UUID id = UUID.randomUUID();

        MazeEntity maze = MazeEntity.builder()
                .id(id)
                .size("15x15")
                .width(15)
                .height(15)
                .layout("maze-layout-data")
                .startX(2)
                .startY(2)
                .goalX(14)
                .goalY(14)
                .build();

        assertEquals(id, maze.getId());
        assertEquals("15x15", maze.getSize());
        assertEquals(15, maze.getWidth());
        assertEquals(15, maze.getHeight());
        assertEquals("maze-layout-data", maze.getLayout());
        assertEquals(2, maze.getStartX());
        assertEquals(2, maze.getStartY());
        assertEquals(14, maze.getGoalX());
        assertEquals(14, maze.getGoalY());
    }

    @Test
    void testSettersAndGetters() {
        MazeEntity maze = new MazeEntity();

        UUID id = UUID.randomUUID();

        maze.setId(id);
        maze.setSize("20x20");
        maze.setWidth(20);
        maze.setHeight(20);
        maze.setLayout("xxxxx");
        maze.setStartX(0);
        maze.setStartY(1);
        maze.setGoalX(19);
        maze.setGoalY(19);

        assertEquals(id, maze.getId());
        assertEquals("20x20", maze.getSize());
        assertEquals(20, maze.getWidth());
        assertEquals(20, maze.getHeight());
        assertEquals("xxxxx", maze.getLayout());
        assertEquals(0, maze.getStartX());
        assertEquals(1, maze.getStartY());
        assertEquals(19, maze.getGoalX());
        assertEquals(19, maze.getGoalY());
    }
}