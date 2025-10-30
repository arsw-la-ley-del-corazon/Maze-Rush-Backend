package org.arsw.maze_rush.maze.entities;

import jakarta.persistence.*;
import lombok.*;
import org.arsw.maze_rush.game.entities.GameEntity;

import java.util.UUID;

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
}
