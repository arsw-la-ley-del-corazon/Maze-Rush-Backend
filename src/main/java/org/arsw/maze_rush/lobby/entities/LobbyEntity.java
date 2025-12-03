package org.arsw.maze_rush.lobby.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.users.entities.UserEntity;

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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Set<LobbyPlayerEntity> lobbyPlayers = new HashSet<>();
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "maze_id")
    private MazeEntity maze;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "EN_ESPERA";
        }
    }

    public void addPlayer(UserEntity user) {
        LobbyPlayerEntity lobbyPlayer = new LobbyPlayerEntity();
        lobbyPlayer.setLobby(this);
        lobbyPlayer.setUser(user);
        lobbyPlayer.setHost(this.creatorUsername.equals(user.getUsername()));
        this.lobbyPlayers.add(lobbyPlayer);
    }

    public void removePlayer(UserEntity user) {
        this.lobbyPlayers.removeIf(lp -> lp.getUser().equals(user));
    }
    
    public Set<UserEntity> getPlayers() {
        return this.lobbyPlayers.stream()
                .map(LobbyPlayerEntity::getUser)
                .collect(Collectors.toSet());
    }




}
