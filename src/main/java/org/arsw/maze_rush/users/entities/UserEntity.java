package org.arsw.maze_rush.users.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password"})
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password; // almacena el hash (BCrypt ~60 chars)

    @Column(name = "score", nullable = false)
    private int score = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

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