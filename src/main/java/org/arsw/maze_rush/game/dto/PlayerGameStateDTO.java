package org.arsw.maze_rush.game.dto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar el estado de un jugador en el juego
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerGameStateDTO {
    private String username;
    private PositionDTO position;
    private Boolean isFinished = false;
    private Long finishTime; // Tiempo en segundos
    private String avatarColor;
    
    public PlayerGameStateDTO(String username, PositionDTO position) {
        this.username = username;
        this.position = position;
        this.isFinished = false;
        this.avatarColor = generateColorForUsername(username);
    }
    
    /**
     * Genera un color consistente para cada jugador basado en su username
     */
    private String generateColorForUsername(String username) {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#90B08C", "#F7DC6F", "#B88FCE"};

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(username.getBytes());

            int value = ((hash[0] & 0xff) << 8) | (hash[1] & 0xff);
            return colors[Math.abs(value) % colors.length];

        } catch (NoSuchAlgorithmException e) {
            return colors[0];
        }
    }

    
}
