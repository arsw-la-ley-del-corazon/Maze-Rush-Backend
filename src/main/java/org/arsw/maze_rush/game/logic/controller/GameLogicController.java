package org.arsw.maze_rush.game.logic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.service.GameLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game-logic")
@Tag(name = "Game Logic (Temporal)", description = "Endpoints temporales para probar la lógica del juego en Maze Rush.")
public class GameLogicController {

    private final GameLogicService gameLogicService;

    public GameLogicController(GameLogicService gameLogicService) {
        this.gameLogicService = gameLogicService;
    }

    @Operation(
        summary = "Inicializar la lógica de un juego",
        description = "Genera el estado inicial de la partida con las posiciones base de cada jugador en el laberinto.",
        parameters = {
            @Parameter(name = "gameId", description = "UUID del juego ya iniciado", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Juego inicializado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameState.class))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/init/{gameId}")
    public ResponseEntity<GameState> initializeGame(@PathVariable UUID gameId) {
        GameState state = gameLogicService.initializeGame(gameId);
        return ResponseEntity.ok(state);
    }

    @Operation(
        summary = "Mover un jugador dentro del juego",
        description = "Permite enviar un movimiento (UP, DOWN, LEFT, RIGHT) de un jugador y actualiza su posición en el laberinto.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Movimiento aplicado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameState.class))),
            @ApiResponse(responseCode = "400", description = "Movimiento inválido o fuera de límites"),
            @ApiResponse(responseCode = "404", description = "Jugador o juego no encontrado")
        }
    )
    @PostMapping("/move/{gameId}")
    public ResponseEntity<GameState> movePlayer(
            @PathVariable UUID gameId,
            @RequestBody PlayerMoveRequestDTO moveRequest) {

        GameState updatedState = gameLogicService.movePlayer(gameId, moveRequest);
        return ResponseEntity.ok(updatedState);
    }

    @Operation(
        summary = "Obtener el estado actual del juego",
        description = "Devuelve las posiciones actuales de los jugadores en el laberinto.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Estado actual obtenido correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameState.class))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado o no inicializado")
        }
    )
    @GetMapping("/state/{gameId}")
    public ResponseEntity<GameState> getCurrentState(@PathVariable UUID gameId) {
        GameState state = gameLogicService.getCurrentState(gameId);
        return ResponseEntity.ok(state);
    }
}
