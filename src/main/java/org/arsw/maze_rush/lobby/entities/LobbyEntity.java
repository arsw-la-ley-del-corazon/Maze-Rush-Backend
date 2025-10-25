package org.arsw.maze_rush.lobby.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;


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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
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
        this.createdAt = LocalDateTime.now();
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
