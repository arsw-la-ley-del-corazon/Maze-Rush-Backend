package org.arsw.maze_rush.game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.arsw.maze_rush.game.dto.GameResponseDTO;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game")
@Tag(name = "Game", description = "Endpoints para la gestión y control de partidas (juegos) dentro de Maze Rush.")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(
        summary = "Iniciar un nuevo juego desde un lobby",
        description = "Crea una partida basada en el lobby con el código proporcionado. " +
                      "Copia los jugadores del lobby, marca el lobby como 'EN_JUEGO' y devuelve los datos del juego inicializado.",
        parameters = {
            @Parameter(
                name = "lobbyCode",
                description = "Código único de 6 caracteres del lobby que se desea iniciar como partida.",
                example = "ABC123",
                required = true
            )
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Juego creado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el lobby con el código especificado"),
            @ApiResponse(responseCode = "409", description = "El lobby ya se encuentra en juego o no cumple condiciones"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/start/{lobbyCode}")
    public ResponseEntity<GameResponseDTO> startGame(
            @PathVariable String lobbyCode) {

        GameEntity game = gameService.startGame(lobbyCode);
        GameResponseDTO response = GameResponseDTO.fromEntity(game);
        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Obtener información de una partida",
        description = "Devuelve los detalles completos de una partida específica usando su UUID.",
        parameters = {
            @Parameter(
                name = "id",
                description = "Identificador único (UUID) del juego.",
                example = "b6f1f1c2-4b32-4a4e-b3c3-1c53e5b8477d",
                required = true
            )
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Juego encontrado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable UUID id) {
        GameEntity game = gameService.getGameById(id);
        GameResponseDTO response = GameResponseDTO.fromEntity(game);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Finalizar un juego en curso",
        description = "Marca una partida como finalizada, asigna la fecha de fin (`finishedAt`) y actualiza el estado del lobby asociado.",
        parameters = {
            @Parameter(
                name = "id",
                description = "Identificador único del juego (UUID).",
                required = true,
                example = "b6f1f1c2-4b32-4a4e-b3c3-1c53e5b8477d"
            )
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Juego finalizado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
            @ApiResponse(responseCode = "409", description = "El juego ya estaba finalizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PutMapping("/finish/{id}")
    public ResponseEntity<GameResponseDTO> finishGame(@PathVariable UUID id) {
        GameEntity finishedGame = gameService.finishGame(id);
        GameResponseDTO response = GameResponseDTO.fromEntity(finishedGame);
        return ResponseEntity.ok(response);
    }
}
