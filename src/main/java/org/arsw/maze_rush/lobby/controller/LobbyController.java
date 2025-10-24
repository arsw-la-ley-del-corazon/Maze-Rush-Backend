package org.arsw.maze_rush.lobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.arsw.maze_rush.lobby.dto.LobbyRequestDTO;
import org.arsw.maze_rush.lobby.dto.LobbyResponseDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lobby")
@Tag(
    name = "Gestión de Lobbies",
    description = "Endpoints para crear, listar, buscar y eliminar salas de juego (lobbies)"
)
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @Operation(
        summary = "Crear un nuevo lobby",
        description = "Crea una nuevo lobby con los parámetros enviados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby creado exitosamente",
                content = @Content(schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o parámetros incorrectos", content = @Content)
        }
    )
    @PostMapping("/create")
    public ResponseEntity<LobbyResponseDTO> createLobby(@Valid @RequestBody LobbyRequestDTO request) {
        LobbyEntity lobby = lobbyService.createLobby(
                request.getMazeSize(),
                request.getMaxPlayers(),
                request.getVisibility(),
                request.getCreatorUsername()
        );

        LobbyResponseDTO response = new LobbyResponseDTO();
        response.setId(lobby.getId().toString());
        response.setCode(lobby.getCode());
        response.setMazeSize(lobby.getMazeSize());
        response.setMaxPlayers(lobby.getMaxPlayers());
        response.setVisibility(lobby.getVisibility());
        response.setCreatorUsername(lobby.getCreatorUsername());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Listar todos los lobbies",
        description = "Devuelve una lista con todas los lobbies registrados en el sistema.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente",
                content = @Content(schema = @Schema(implementation = LobbyResponseDTO.class))),
        }
    )
    @GetMapping("")
    public ResponseEntity<List<LobbyResponseDTO>> getAllLobbies() {
        List<LobbyResponseDTO> response = lobbyService.getAllLobbies().stream().map(lobby -> {
            LobbyResponseDTO dto = new LobbyResponseDTO();
            dto.setId(lobby.getId().toString());
            dto.setCode(lobby.getCode());
            dto.setMazeSize(lobby.getMazeSize());
            dto.setMaxPlayers(lobby.getMaxPlayers());
            dto.setVisibility(lobby.getVisibility());
            dto.setCreatorUsername(lobby.getCreatorUsername());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar un lobby por código",
        description = "Obtiene la información de un lobby específico usando su código.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby encontrado",
                content = @Content(schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lobby no encontrado", content = @Content)
        }
    )
    @GetMapping("/{code}")
    public ResponseEntity<LobbyResponseDTO> getLobbyByCode(@PathVariable String code) {
        LobbyEntity lobby = lobbyService.getLobbyByCode(code);

        LobbyResponseDTO response = new LobbyResponseDTO();
        response.setId(lobby.getId().toString());
        response.setCode(lobby.getCode());
        response.setMazeSize(lobby.getMazeSize());
        response.setMaxPlayers(lobby.getMaxPlayers());
        response.setVisibility(lobby.getVisibility());
        response.setCreatorUsername(lobby.getCreatorUsername());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Eliminar un lobby",
        description = "Elimina un lobby del sistema mediante su ID (solo el creador debería tener permiso).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Lobby no encontrado", content = @Content)
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLobby(@PathVariable UUID id) {
        lobbyService.deleteLobby(id);
        return ResponseEntity.ok("Lobby eliminado correctamente");
    }
}
