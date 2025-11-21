package org.arsw.maze_rush.maze.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "mazes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MazeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String size; 

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Lob
    @Column(nullable = false)
    private String layout; 

    @Column(nullable = false)
    private int startX;

    @Column(nullable = false)
    private int startY;

    @Column(nullable = false)
    private int goalX;

    @Column(nullable = false)
    private int goalY;

    public static final int PU_SPEED = 2;
    public static final int PU_SCORE = 3;
    public static final int PU_LIFE = 4;

    public static final int WALL = 1;
    public static final int EMPTY = 0;

    public static final int[] POWER_UP_TYPES = { PU_SPEED, PU_SCORE, PU_LIFE };

    public boolean placePowerUp(int x, int y, int type) {
        int[][] matrix = getLayoutMatrix();

        if (matrix[x][y] == EMPTY) {
            matrix[x][y] = type;
            setLayoutMatrix(matrix);
            return true;
        }

        return false;
    }


    @Transient
    private static final ObjectMapper mapper = new ObjectMapper();
    public int[][] getLayoutMatrix() {
        try {
            return mapper.readValue(this.layout, int[][].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing maze layout JSON", e);
        }
    }

    public void setLayoutMatrix(int[][] matrix) {
        try {
            this.layout = mapper.writeValueAsString(matrix);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing layout JSON", e);
        }
    }
}
