package org.arsw.maze_rush.maze.entities;

import org.arsw.maze_rush.common.exceptions.InvalidMazeJsonException;
import org.arsw.maze_rush.common.exceptions.MazeJsonWriteException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MazeEntityTest {

    @Test
    void getLayoutMatrix_ShouldParseValidJson() {

        String jsonLayout = """
                [[0,0,0],
                 [0,1,0],
                 [0,0,0]]
                """;

        MazeEntity maze = MazeEntity.builder()
                .width(3)
                .height(3)
                .layout(jsonLayout)
                .build();

        int[][] matrix = maze.getLayoutMatrix();

        assertEquals(0, matrix[0][0]);
        assertEquals(1, matrix[1][1]);
        assertEquals(3, matrix.length);
        assertEquals(3, matrix[0].length);
    }

    @Test
    void getLayoutMatrix_ShouldThrowInvalidJsonException() {

        MazeEntity maze = MazeEntity.builder()
                .width(3)
                .height(3)
                .layout("INVALID JSON")
                .build();

        assertThrows(InvalidMazeJsonException.class, maze::getLayoutMatrix);
    }

    @Test
    void setLayoutMatrix_ShouldWriteValidJson() {

        int[][] matrix = {
                {0, 1},
                {1, 0}
        };

        MazeEntity maze = MazeEntity.builder()
                .width(2)
                .height(2)
                .build();

        maze.setLayoutMatrix(matrix);
        int[][] result = maze.getLayoutMatrix();

        assertArrayEquals(matrix[0], result[0]);
        assertArrayEquals(matrix[1], result[1]);
    }

    @Test
    void setLayoutMatrix_ShouldThrowWriteException_WhenJsonFails() {

        MazeEntity maze = MazeEntity.builder().build();

        int[][] invalidMatrix = new int[3][];
        invalidMatrix[0] = new int[]{1};
        invalidMatrix[1] = null;    
        invalidMatrix[2] = new int[]{0};

        assertThrows(MazeJsonWriteException.class,
                () -> maze.setLayoutMatrix(invalidMatrix)
        );
    }

    @Test
    void placePowerUp_ShouldPlace_WhenCellIsEmpty() {

        String jsonLayout = """
                [[0,0,0],
                 [0,0,0],
                 [0,0,0]]
                """;

        MazeEntity maze = MazeEntity.builder()
                .layout(jsonLayout)
                .width(3)
                .height(3)
                .build();

        boolean placed = maze.placePowerUp(1, 1, MazeEntity.PU_SPEED);

        assertTrue(placed);

        int[][] updated = maze.getLayoutMatrix();
        assertEquals(MazeEntity.PU_SPEED, updated[1][1]);
    }

    @Test
    void placePowerUp_ShouldNotPlace_WhenCellIsWall() {

        String jsonLayout = """
                [[0,1,0],
                 [0,0,0],
                 [0,0,0]]
                """;

        MazeEntity maze = MazeEntity.builder()
                .layout(jsonLayout)
                .width(3)
                .height(3)
                .build();

        boolean placed = maze.placePowerUp(1, 0, MazeEntity.PU_LIFE);

        assertFalse(placed);

        int[][] updated = maze.getLayoutMatrix();
        assertEquals(1, updated[0][1]); 
    }

    @Test
    void placePowerUp_ShouldNotPlace_WhenAlreadyHasPowerUp() {

        String jsonLayout = """
                [[0,0,0],
                 [0,3,0],
                 [0,0,0]]
                """;

        MazeEntity maze = MazeEntity.builder()
                .layout(jsonLayout)
                .width(3)
                .height(3)
                .build();

        boolean placed = maze.placePowerUp(1, 1, MazeEntity.PU_SPEED);

        assertFalse(placed);

        int[][] updated = maze.getLayoutMatrix();
        assertEquals(3, updated[1][1]);
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
