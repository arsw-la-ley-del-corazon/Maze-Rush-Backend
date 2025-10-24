package org.arsw.maze_rush.lobby.entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;


import org.arsw.maze_rush.users.entities.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



/**
 * Entidad JPA que representa un lobby (sala de juego) dentro del sistema Maze Rush.
 *
 * <p>Cada lobby contiene información sobre su tamaño de laberinto,
 * número máximo de jugadores, visibilidad, estado actual y el usuario creador.</p>
 *
 * <h3>Características:</h3>
 * <ul>
 *   <li>Identificador único tipo {@link UUID} generado automáticamente.</li>
 *   <li>Código alfanumérico único de 6 caracteres para acceder al lobby.</li>
 *   <li>Campos de visibilidad y estado gestionados automáticamente al crear la entidad.</li>
 * </ul>
 *
 */
@Entity
@Table(name = "lobbies")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LobbyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false, length = 50)
    private String creatorUsername;

    @Column(nullable = false, length = 20)
    private String mazeSize;

    @Column(nullable = false)
    private int maxPlayers;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @ManyToMany
    @JoinTable(
        name = "lobby_players",
        joinColumns = @JoinColumn(name = "lobby_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Set<UserEntity> players = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "EN_ESPERA";
        }
    }

    public void addPlayer(UserEntity user) {
    this.players.add(user);
    user.getLobbies().add(this);
    }

    public void removePlayer(UserEntity user) {
        this.players.remove(user);
        user.getLobbies().remove(this);
    }




}
