package org.arsw.maze_rush.maze.entities;

import org.arsw.maze_rush.common.exceptions.InvalidMazeJsonException;
import org.arsw.maze_rush.common.exceptions.MazeJsonWriteException;
import org.junit.jupiter.api.Test;

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
    }
}
