package org.arsw.maze_rush.lobby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.common.ApiError;
import org.arsw.maze_rush.lobby.dto.CreateLobbyRequestDTO;
import org.arsw.maze_rush.lobby.dto.LobbyResponseDTO;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de lobby
 */
@RestController
@RequestMapping("/api/v1/lobby")
@Tag(name = "Lobby", description = "Endpoints para gestión de salas de espera")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @Operation(
        summary = "Crear nuevo lobby",
        description = "Crea una nueva sala de espera para jugar"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Lobby creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LobbyResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/create")
    public ResponseEntity<LobbyResponseDTO> createLobby(
            @Valid @RequestBody CreateLobbyRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creando lobby para usuario: {}", userDetails.getUsername());
        LobbyResponseDTO response = lobbyService.createLobby(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Unirse a un lobby",
        description = "Permite unirse a una sala existente usando su código"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Unido al lobby exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LobbyResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lobby no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/join/{code}")
    public ResponseEntity<LobbyResponseDTO> joinLobby(
            @Parameter(description = "Código del lobby") @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Usuario {} uniéndose al lobby: {}", userDetails.getUsername(), code);
        LobbyResponseDTO response = lobbyService.joinLobby(code, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener información del lobby",
        description = "Obtiene los detalles y jugadores de un lobby"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LobbyResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lobby no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @GetMapping("/{code}")
    public ResponseEntity<LobbyResponseDTO> getLobby(
            @Parameter(description = "Código del lobby") @PathVariable String code) {
        LobbyResponseDTO response = lobbyService.getLobby(code);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Salir del lobby",
        description = "Permite a un jugador salir de la sala"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Salida exitosa"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lobby o jugador no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping("/leave/{code}")
    public ResponseEntity<Void> leaveLobby(
            @Parameter(description = "Código del lobby") @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Usuario {} saliendo del lobby: {}", userDetails.getUsername(), code);
        lobbyService.leaveLobby(code, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
