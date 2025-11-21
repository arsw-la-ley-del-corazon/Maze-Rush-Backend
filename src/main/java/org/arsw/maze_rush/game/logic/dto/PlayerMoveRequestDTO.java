package org.arsw.maze_rush.game.logic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud de movimiento del jugador dentro del juego.")
public class PlayerMoveRequestDTO {

    @Schema(description = "Nombre del jugador que realiza el movimiento.", example = "sebastian")
    private String username;

    @Schema(description = "Dirección de movimiento (UP, DOWN, LEFT, RIGHT).", example = "UP")
    private String direction;
}
