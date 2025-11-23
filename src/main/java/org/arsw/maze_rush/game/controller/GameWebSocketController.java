package org.arsw.maze_rush.game.controller;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.game.dto.*;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.service.MazeService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador WebSocket para eventos de juego en tiempo real
 * Maneja movimientos, finalización y sincronización de estado entre jugadores
 */
@Controller
@Slf4j
public class GameWebSocketController {

    private final GameSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final MazeService mazeService;

    public GameWebSocketController(GameSessionManager sessionManager, 
                                   SimpMessagingTemplate messagingTemplate,
                                   MazeService mazeService) {
        this.sessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
        this.mazeService = mazeService;
    }

    /**
     * Maneja cuando un jugador se une al juego
     * Los clientes envían a: /app/game/{lobbyCode}/join
     * Respuesta broadcast a: /topic/game/{lobbyCode}/move
     */
    @MessageMapping("/game/{lobbyCode}/join")
    public void handlePlayerJoin(
            @DestinationVariable String lobbyCode,
            @Payload Map<String, String> payload,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String username = extractUsername(payload, principal);
        if (username == null) {
            log.warn("Username no proporcionado en join para lobby {}", lobbyCode);
            return;
        }

        // Registrar jugador en la sesión
        sessionManager.addPlayer(lobbyCode, username);

        // Guardar información en la sesión WebSocket
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", username);
            headerAccessor.getSessionAttributes().put("lobbyCode", lobbyCode);
        }

        // Notificar a todos los jugadores que alguien se unió
        GameEventDTO event = new GameEventDTO("player_joined", username);
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/move", event);

        // Enviar sincronización completa al nuevo jugador
        sendSync(lobbyCode);

        log.info("Jugador {} se unió al juego {}", username, lobbyCode);
    }

    /**
     * Maneja movimientos de jugadores
     * Los clientes envían a: /app/game/{lobbyCode}/move
     * Respuesta broadcast a: /topic/game/{lobbyCode}/move
     */
    @MessageMapping("/game/{lobbyCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String lobbyCode,
            @Payload Map<String, Object> payload,
            Principal principal) {
        
        String username = extractUsername(payload, principal);
        if (username == null) {
            log.warn("Username no proporcionado en move para lobby {}", lobbyCode);
            return;
        }

        // Extraer posición del payload
        @SuppressWarnings("unchecked")
        Map<String, Integer> positionMap = (Map<String, Integer>) payload.get("position");
        if (positionMap == null) {
            log.warn("Posición no proporcionada en move para {} en {}", username, lobbyCode);
            return;
        }

        PositionDTO position = new PositionDTO(
            positionMap.get("x"),
            positionMap.get("y")
        );

        // Actualizar posición en la sesión
        sessionManager.updatePlayerPosition(lobbyCode, username, position);

        // Broadcast del movimiento a todos los jugadores
        GameMoveDTO moveEvent = new GameMoveDTO(username, position);
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/move", moveEvent);

        log.debug("Movimiento de {} en {}: ({}, {})", 
            username, lobbyCode, position.getX(), position.getY());
    }

    /**
     * Maneja cuando un jugador termina el laberinto
     * Los clientes envían a: /app/game/{lobbyCode}/finish
     * Respuesta broadcast a: /topic/game/{lobbyCode}/move
     */
    @MessageMapping("/game/{lobbyCode}/finish")
    public void handlePlayerFinish(
            @DestinationVariable String lobbyCode,
            @Payload Map<String, Object> payload,
            Principal principal) {
        
        String username = extractUsername(payload, principal);
        if (username == null) {
            log.warn("Username no proporcionado en finish para lobby {}", lobbyCode);
            return;
        }

        // Marcar jugador como finalizado (calcula el tiempo internamente)
        sessionManager.markPlayerFinished(lobbyCode, username);

        // Obtener tiempo para el broadcast
        Long finishTime = sessionManager.getPlayer(lobbyCode, username) != null
            ? sessionManager.getPlayer(lobbyCode, username).getFinishTime()
            : 0L;

        // Broadcast del evento de finalización
        GameFinishDTO finishEvent = new GameFinishDTO(username, finishTime);
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/move", finishEvent);

        log.info("Jugador {} terminó el juego {} en {} segundos", 
            username, lobbyCode, finishTime);

        // Verificar si todos terminaron
        checkAllPlayersFinished(lobbyCode);
    }

    /**
     * Maneja cuando un jugador sale del juego
     * Los clientes envían a: /app/game/{lobbyCode}/leave
     * Respuesta broadcast a: /topic/game/{lobbyCode}/move
     */
    @MessageMapping("/game/{lobbyCode}/leave")
    public void handlePlayerLeave(
            @DestinationVariable String lobbyCode,
            @Payload Map<String, String> payload,
            Principal principal) {
        
        String username = extractUsername(payload, principal);
        if (username == null) {
            log.warn("Username no proporcionado en leave para lobby {}", lobbyCode);
            return;
        }

        // Remover jugador de la sesión
        sessionManager.removePlayer(lobbyCode, username);

        // Notificar a todos que el jugador salió
        GameEventDTO event = new GameEventDTO("player_left", username);
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/move", event);

        log.info("Jugador {} salió del juego {}", username, lobbyCode);
    }

    /**
     * Envía sincronización completa del estado del juego
     */
    private void sendSync(String lobbyCode) {
        List<PlayerGameStateDTO> players = sessionManager.getPlayers(lobbyCode);
        if (!players.isEmpty()) {
            GameSyncDTO syncData = new GameSyncDTO(players);
            messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/sync", syncData);
            log.debug("Sincronización enviada para lobby {} ({} jugadores)", 
                lobbyCode, players.size());
        }
    }

    /**
     * Verifica si todos los jugadores terminaron el juego
     */
    private void checkAllPlayersFinished(String lobbyCode) {
        List<PlayerGameStateDTO> players = sessionManager.getPlayers(lobbyCode);
        boolean allFinished = !players.isEmpty() && 
            players.stream().allMatch(PlayerGameStateDTO::getIsFinished);

        if (allFinished) {
            log.info("Todos los jugadores terminaron el juego {}", lobbyCode);
            // Aquí podrías actualizar el estado del juego en la base de datos
            // o realizar otras acciones necesarias
        }
    }

    /**
     * Extrae el username del payload o del principal
     */
    private String extractUsername(Map<String, ?> payload, Principal principal) {
        String username = (String) payload.get("username");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        return username;
    }
}
