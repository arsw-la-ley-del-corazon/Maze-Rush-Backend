package org.arsw.maze_rush.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arsw.maze_rush.powerups.entities.PowerUpType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameNotificationDTO {
    private String type;     
    private String message;   
    private String sourceUser; 
    private PowerUpType powerUpType;
}