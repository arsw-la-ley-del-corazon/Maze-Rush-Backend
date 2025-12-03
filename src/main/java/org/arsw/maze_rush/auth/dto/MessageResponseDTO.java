package org.arsw.maze_rush.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta genérica para mensajes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private String message;
}
