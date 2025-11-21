package org.arsw.maze_rush.game.entities;

import jakarta.persistence.*;
import lombok.*;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.maze.entities.MazeEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "lobby_id", nullable = false, unique = true)
    private LobbyEntity lobby;

    @ManyToMany
    @JoinTable(
        name = "game_players",
        joinColumns = @JoinColumn(name = "game_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserEntity> players = new HashSet<>();

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "EN_CURSO";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime finishedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "maze_id")
    private MazeEntity maze;


    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }



}
