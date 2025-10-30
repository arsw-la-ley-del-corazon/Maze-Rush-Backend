package org.arsw.maze_rush.lobby.entities;

import jakarta.persistence.*;
import org.arsw.maze_rush.users.entities.UserEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un lobby de juego
 * Un lobby es una sala de espera donde los jugadores se reúnen antes de iniciar una partida
 */
@Entity
@Table(name = "lobbies")
public class LobbyEntity {
    
    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 6)
    private String code; // Código de 6 caracteres para unirse al lobby

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserEntity host; // Jugador que creó el lobby

    @Column(name = "max_players", nullable = false)
    private int maxPlayers = 4;

    @Column(name = "is_private", nullable = false)
    private boolean privateMode = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LobbyStatus status = LobbyStatus.WAITING;

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LobbyPlayerEntity> players = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LobbyEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UserEntity getHost() {
        return host;
    }

    public void setHost(UserEntity host) {
        this.host = host;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isPrivate() {
        return privateMode;
    }

    public void setPrivate(boolean privateMode) {
        this.privateMode = privateMode;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    public List<LobbyPlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<LobbyPlayerEntity> players) {
        this.players = players;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Verifica si el lobby está lleno
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    /**
     * Verifica si todos los jugadores están listos
     */
    public boolean areAllPlayersReady() {
        if (players.isEmpty() || players.size() < 2) {
            return false;
        }
        return players.stream().allMatch(LobbyPlayerEntity::isReady);
    }

    /**
     * Obtiene el número actual de jugadores
     */
    public int getPlayerCount() {
        return players.size();
    }
}
