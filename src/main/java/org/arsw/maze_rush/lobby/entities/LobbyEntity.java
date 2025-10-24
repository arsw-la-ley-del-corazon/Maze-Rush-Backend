package org.arsw.maze_rush.lobby.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "lobbies")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LobbyEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 6)
    private String code;

    @Column(name = "maze_size", nullable = false, length = 20)
    private String mazeSize;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility;

    @Column(name = "creator_username", nullable = false, length = 50)
    private String creatorUsername;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
