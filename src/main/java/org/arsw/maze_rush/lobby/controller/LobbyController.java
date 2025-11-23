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
import org.arsw.maze_rush.lobby.dto.LobbyWithPlayersResponseDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyWithPlayersResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o parámetros fuera de rango"),
            @ApiResponse(responseCode = "401", description = "No autorizado (token inválido o ausente)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @PostMapping("/create")
    public ResponseEntity<LobbyWithPlayersResponseDTO> createLobby(
            @Valid @RequestBody
            @Parameter(description = "Datos necesarios para crear un lobby", required = true)
            LobbyRequestDTO request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String creatorUsername = authentication.getName();

        LobbyEntity lobby = lobbyService.createLobby(
            request.getMazeSize(),
            request.getMaxPlayers(),
            request.isPublic(),
            request.getStatus(),
            creatorUsername
        );

        LobbyWithPlayersResponseDTO response = new LobbyWithPlayersResponseDTO();
        response.setId(lobby.getId().toString());
        response.setCode(lobby.getCode());
        response.setMazeSize(lobby.getMazeSize());
        response.setMaxPlayers(lobby.getMaxPlayers());
        response.setPublic(lobby.isPublic());
        response.setStatus(lobby.getStatus());
        response.setCreatorUsername(lobby.getCreatorUsername());
        response.setCreatedAt(lobby.getCreatedAt().toString());
        response.setPlayers(
            lobby.getPlayers().stream()
                    .map(UserEntity::getUsername)
                    .toList()
        );

        return ResponseEntity.ok(response);
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
        summary = "Obtener detalles completos del lobby",
        description = "Retorna toda la información del lobby, incluyendo la lista de jugadores conectados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lobby encontrado correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró un lobby con el código especificado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        }
    )
    @GetMapping("/{code}")
    public ResponseEntity<LobbyWithPlayersResponseDTO> getLobbyByCode(
            @Parameter(description = "Código único de 6 caracteres del lobby", example = "AB12CD")
            @PathVariable String code) {

        LobbyEntity lobby = lobbyService.getLobbyByCode(code);

        LobbyWithPlayersResponseDTO response = new LobbyWithPlayersResponseDTO();
        response.setId(lobby.getId().toString());
        response.setCode(lobby.getCode());
        response.setMazeSize(lobby.getMazeSize());
        response.setMaxPlayers(lobby.getMaxPlayers());
        response.setPublic(lobby.isPublic());
        response.setStatus(lobby.getStatus());
        response.setCreatorUsername(lobby.getCreatorUsername());
        response.setCreatedAt(lobby.getCreatedAt().toString());
        response.setPlayers(
            lobby.getPlayers().stream()
                    .map(player -> player.getUsername())
                    .toList()
        );

        return ResponseEntity.ok(response);
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


    @Operation(
        summary = "Unirse a un lobby usando su código",
        description = "Permite a un usuario unirse a un lobby existente usando el código del lobby."
    )
    @PostMapping("/join/{code}")
    public ResponseEntity<LobbyWithPlayersResponseDTO> joinLobby(
            @PathVariable String code) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        LobbyEntity lobby = lobbyService.joinLobbyByCode(code,username);

        LobbyWithPlayersResponseDTO response = new LobbyWithPlayersResponseDTO();
        response.setId(lobby.getId().toString());
        response.setCode(lobby.getCode());
        response.setMazeSize(lobby.getMazeSize());
        response.setMaxPlayers(lobby.getMaxPlayers());
        response.setPublic(lobby.isPublic());
        response.setStatus(lobby.getStatus());
        response.setCreatorUsername(lobby.getCreatorUsername());
        response.setCreatedAt(lobby.getCreatedAt().toString());
        response.setPlayers(
            lobby.getPlayers().stream()
                    .map(UserEntity::getUsername)
                    .toList()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Salir de un lobby",
        description = "Permite a un jugador salir del lobby actual. Si es el último jugador, el lobby se marca como ABANDONADO."
    )
    @DeleteMapping("/{code}/leave")
    public ResponseEntity<String> leaveLobby(@PathVariable String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        lobbyService.leaveLobby(code, username);

        return ResponseEntity.ok("El jugador " + username + " ha salido del lobby " + code);
    }





    
    private LobbyResponseDTO mapToDTO(LobbyEntity lobby) {
        LobbyResponseDTO dto = new LobbyResponseDTO();
        dto.setId(lobby.getId().toString());
        dto.setCode(lobby.getCode());
        dto.setMazeSize(lobby.getMazeSize());
        dto.setMaxPlayers(lobby.getMaxPlayers());
        dto.setCurrentPlayers(lobby.getPlayers().size());  // Número actual de jugadores
        dto.setPublic(lobby.isPublic());  // Lombok genera isPublic() para boolean, y setPublic() en el DTO
        dto.setStatus(lobby.getStatus());
        dto.setCreatorUsername(lobby.getCreatorUsername());
        dto.setCreatedAt(lobby.getCreatedAt().toString());
        return dto;
    }




}
