package org.arsw.maze_rush.lobby.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para crear un nuevo lobby
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLobbyRequestDTO {
    
    @NotNull(message = "El número máximo de jugadores es requerido")
    @Min(value = 2, message = "Debe haber al menos 2 jugadores")
    @Max(value = 8, message = "El máximo es 8 jugadores")
    private Integer maxPlayers = 4;
    
    @NotNull(message = "El modo es requerido")
    private Boolean isPrivate = true;
}
