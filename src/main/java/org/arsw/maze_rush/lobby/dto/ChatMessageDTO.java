package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para mensajes de chat en el lobby
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String username;
    private String message;
    private Instant timestamp;
    
    public ChatMessageDTO(String username, String message) {
        this.username = username;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
