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
    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 20)
    private String size; 

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Lob
    @Column(nullable = false)
    private String layout;

    @OneToOne(mappedBy = "maze")
    private GameEntity game;
}
