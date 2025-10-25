package org.arsw.maze_rush.lobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.arsw.maze_rush.lobby.dto.LobbyRequestDTO;
import org.arsw.maze_rush.lobby.dto.LobbyResponseDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lobby")
@Tag(
    name = "Lobbies",
    description = "Endpoints para la creación, consulta y gestión de salas (lobbies) de juego."
)
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

   
    @Operation(
        summary = "Crear un nuevo lobby",
        description = "Permite crear una nueva sala de juego (lobby) especificando el tamaño del laberinto, visibilidad y número máximo de jugadores.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby creado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o parámetros fuera de rango"),
            @ApiResponse(responseCode = "401", description = "No autorizado (token inválido o ausente)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/create")
    public ResponseEntity<LobbyResponseDTO> createLobby(
            @Valid @RequestBody
            @Parameter(description = "Datos necesarios para crear un lobby", required = true)
            LobbyRequestDTO request) {

        LobbyEntity lobby = lobbyService.createLobby(
            request.getMazeSize(),
            request.getMaxPlayers(),
            request.isPublic(),
            request.getStatus(),
            request.getCreatorUsername()
        );

        return ResponseEntity.ok(mapToDTO(lobby));
    }


    @Operation(
        summary = "Listar todos los lobbies",
        description = "Obtiene la lista completa de lobbies creados en el sistema.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de lobbies obtenida correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @GetMapping("/all")
    public ResponseEntity<List<LobbyResponseDTO>> getAllLobbies() {
        List<LobbyResponseDTO> response = lobbyService.getAllLobbies()
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    
    @Operation(
        summary = "Obtener un lobby por su código",
        description = "Busca y retorna un lobby específico a partir de su código único de 6 caracteres.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby encontrado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró un lobby con el código especificado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @GetMapping("/{code}")
    public ResponseEntity<LobbyResponseDTO> getLobbyByCode(
            @Parameter(description = "Código único de 6 caracteres del lobby", example = "AB12CD")
            @PathVariable String code) {

        LobbyEntity lobby = lobbyService.getLobbyByCode(code);
        return ResponseEntity.ok(mapToDTO(lobby));
    }


    @Operation(
        summary = "Eliminar un lobby",
        description = "Permite eliminar un lobby existente utilizando su identificador único (UUID).",
        responses = {
            @ApiResponse(responseCode = "204", description = "Lobby eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el lobby especificado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLobby(
            @Parameter(description = "Identificador único del lobby (UUID)", example = "8f7d1f08-2b6d-4c6a-b4b8-7e4e82b8f5c3")
            @PathVariable UUID id) {

        lobbyService.deleteLobby(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

   
    @Operation(
        summary = "Agregar jugador a un lobby",
        description = "Permite asociar un usuario existente a un lobby mediante sus identificadores UUID."
    )
    @PostMapping("/{lobbyId}/add-player/{userId}")
    public ResponseEntity<String> addPlayerToLobby(
            @PathVariable UUID lobbyId,
            @PathVariable UUID userId) {
        lobbyService.addPlayerToLobby(lobbyId, userId);
        return ResponseEntity.ok("Jugador agregado correctamente al lobby");
    }

    
    @Operation(
        summary = "Remover jugador de un lobby",
        description = "Elimina la relación entre un usuario y un lobby existente."
    )
    @DeleteMapping("/{lobbyId}/remove-player/{userId}")
    public ResponseEntity<String> removePlayerFromLobby(
            @PathVariable UUID lobbyId,
            @PathVariable UUID userId) {
        lobbyService.removePlayerFromLobby(lobbyId, userId);
        return ResponseEntity.ok("Jugador removido correctamente del lobby");
    }

    
    private LobbyResponseDTO mapToDTO(LobbyEntity lobby) {
        LobbyResponseDTO dto = new LobbyResponseDTO();
        dto.setId(lobby.getId().toString());
        dto.setCode(lobby.getCode());
        dto.setMazeSize(lobby.getMazeSize());
        dto.setMaxPlayers(lobby.getMaxPlayers());
        dto.setPublic(lobby.isPublic());
        dto.setStatus(lobby.getStatus());
        dto.setCreatorUsername(lobby.getCreatorUsername());
        dto.setCreatedAt(lobby.getCreatedAt().toString());
        return dto;
    }
}
