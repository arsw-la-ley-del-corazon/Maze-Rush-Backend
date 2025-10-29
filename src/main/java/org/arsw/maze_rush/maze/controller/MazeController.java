package org.arsw.maze_rush.maze.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.service.MazeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/map")
@Tag(name = "Maze", description = "Endpoints para la generación y consulta de laberintos del juego.")
public class MazeController {

    private final MazeService mazeService;

    public MazeController(MazeService mazeService) {
        this.mazeService = mazeService;
    }

    @Operation(
        summary = "Generar un nuevo laberinto",
        description = "Crea un laberinto aleatorio con un tamaño predefinido (SMALL, MEDIUM, LARGE).",
        parameters = {
            @Parameter(name = "size", description = "Tamaño del laberinto (SMALL, MEDIUM, LARGE)", example = "MEDIUM", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Laberinto generado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = MazeEntity.class))),
            @ApiResponse(responseCode = "400", description = "Tamaño inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/generate/{size}")
    public ResponseEntity<MazeEntity> generateMaze(@PathVariable String size) {
        MazeEntity maze = mazeService.generateMaze(size);
        return ResponseEntity.ok(maze);
    }

    @Operation(
        summary = "Obtener un laberinto por ID",
        description = "Devuelve los datos de un laberinto específico, incluyendo su tamaño y estructura JSON.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Laberinto encontrado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = MazeEntity.class))),
            @ApiResponse(responseCode = "404", description = "Laberinto no encontrado")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MazeEntity> getMazeById(@PathVariable UUID id) {
        MazeEntity maze = mazeService.getMazeById(id);
        return ResponseEntity.ok(maze);
    }
}
