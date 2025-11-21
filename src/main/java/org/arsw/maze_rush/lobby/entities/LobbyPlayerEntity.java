package org.arsw.maze_rush.lobby.entities;

import jakarta.persistence.*;
import org.arsw.maze_rush.users.entities.UserEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad que representa a un jugador dentro de un lobby
 * Contiene información sobre el estado del jugador en la sala de espera
 */
@Entity
@Table(name = "lobby_players")
public class LobbyPlayerEntity {
    
    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_id", nullable = false)
    private LobbyEntity lobby;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "is_ready", nullable = false)
    private boolean ready = false;

    @Column(name = "is_host", nullable = false)
    private boolean host = false;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public LobbyPlayerEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.joinedAt = now;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LobbyEntity getLobby() {
        return lobby;
    }

    public void setLobby(LobbyEntity lobby) {
        this.lobby = lobby;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
