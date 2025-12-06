package org.arsw.maze_rush.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.arsw.maze_rush.common.ApiError;
import org.arsw.maze_rush.users.dto.UserStatsDTO;
import org.arsw.maze_rush.users.service.UserStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Statistics", description = "Endpoints para consultar el desempeño histórico de los jugadores.")
public class UserStatsController {

    private final UserStatsService userStatsService;

    @Operation(
        summary = "Obtener estadísticas de un usuario",
        description = "Devuelve el historial acumulado (partidas jugadas, ganadas, mejor tiempo, etc.) de un jugador específico.",
        parameters = {
            @Parameter(name = "username", description = "Nombre de usuario a consultar", required = true, example = "player1")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserStatsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        }
    )
    @GetMapping("/{username}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable String username) {
        UserStatsDTO stats = userStatsService.getStats(username);
        return ResponseEntity.ok(stats);
    }
}