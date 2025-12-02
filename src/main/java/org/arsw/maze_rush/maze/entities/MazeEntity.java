package org.arsw.maze_rush.maze.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import org.arsw.maze_rush.common.exceptions.InvalidMazeJsonException;
import org.arsw.maze_rush.common.exceptions.MazeJsonWriteException;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public static final List<Integer> POWER_UP_TYPES =  List.of(PU_SPEED, PU_SCORE, PU_LIFE);

    public boolean placePowerUp(int x, int y, int type) {
        int[][] matrix = getLayoutMatrix();

        if (matrix[y][x] == EMPTY) {
            matrix[y][x] = type;
            setLayoutMatrix(matrix);
            return true;
        }

        return false;
    }


    @Transient
    private static final ObjectMapper mapper = new ObjectMapper();
    @JsonIgnore
    public int[][] getLayoutMatrix() {
        try {
            return mapper.readValue(this.layout, int[][].class);
        } catch (JsonProcessingException e) {
            throw new InvalidMazeJsonException("Error parsing maze layout JSON", e);
        }
    }
    @JsonIgnore
    public void setLayoutMatrix(int[][] matrix) {
        if (matrix == null) throw new MazeJsonWriteException("Matrix is null",null);

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new MazeJsonWriteException("Matrix row " + i + " is null",null);
            }
        }

        try {
            this.layout = mapper.writeValueAsString(matrix);
        } catch (JsonProcessingException e) {
            throw new MazeJsonWriteException("Error writing layout JSON", e);
        }
    }


}
