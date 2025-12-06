package org.arsw.maze_rush.users.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Builder.Default
    private Integer gamesPlayed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer gamesWon = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer powerUpsCollected = 0;

    @Column(name = "fastest_time_ms")
    private Long fastestTimeMs;

    public void updateFastestTime(long newTimeMs) {
        if (this.fastestTimeMs == null || newTimeMs < this.fastestTimeMs) {
            this.fastestTimeMs = newTimeMs;
        }
    }
}