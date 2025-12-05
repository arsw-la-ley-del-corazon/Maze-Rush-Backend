package org.arsw.maze_rush.game.controller;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.game.dto.*;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.service.GameLogicService;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador WebSocket para eventos de juego en tiempo real
 * Maneja movimientos, finalización y sincronización de estado entre jugadores
 */
@Controller
@Slf4j
public class GameWebSocketController {
    private static final String TOPIC_GAME_PREFIX = "/topic/game/";
    private static final String MOVE_SUFFIX = "/move";
    private static final String SYNC_SUFFIX = "/sync";

    private final GameSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameLogicService gameLogicService;
    
    public GameWebSocketController(GameSessionManager sessionManager, 
                                   SimpMessagingTemplate messagingTemplate,
                                   GameLogicService gameLogicService) { 
        this.sessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
        this.gameLogicService = gameLogicService; 
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
        messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX + lobbyCode + MOVE_SUFFIX , event);

        // Enviar sincronización completa al nuevo jugador
        sendSync(lobbyCode);

        log.info("Jugador {} se unió al juego {}", username, lobbyCode);
    }

    /**
     * Maneja movimientos de jugadores
     * Los clientes envían a: /app/game/{lobbyCode}/move
     * Respuesta broadcast a: /topic/game/{lobbyCode}/move
     * 
     * Acepta dos formatos:
     * 1. {username, position: {x, y}} - posición directa del frontend
     * 2. {username, direction, gameId} - para lógica compleja con power-ups
     */
    @MessageMapping("/game/{lobbyCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String lobbyCode,
            @Payload Map<String, Object> payload,
            Principal principal) {
        
        String username = extractUsername(payload, principal);
        if (username == null) return;

        // Verificar si viene posición directa del frontend
        @SuppressWarnings("unchecked")
        Map<String, Object> positionMap = (Map<String, Object>) payload.get("position");
        
        if (positionMap != null) {
            // Modo simple: actualizar posición directamente
            try {
                int x = ((Number) positionMap.get("x")).intValue();
                int y = ((Number) positionMap.get("y")).intValue();
                
                PositionDTO newPosition = new PositionDTO(x, y);
                sessionManager.updatePlayerPosition(lobbyCode, username, newPosition);
                
                // Crear evento de movimiento para broadcast
                PlayerGameStateDTO playerState = sessionManager.getPlayer(lobbyCode, username);
                if (playerState != null) {
                    GameMoveDTO moveEvent = new GameMoveDTO(username, newPosition);
                    messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX + lobbyCode + MOVE_SUFFIX, moveEvent);
                    log.debug("Movimiento broadcast para {} en {}: ({}, {})", username, lobbyCode, x, y);
                }
                return;
            } catch (Exception e) {
                log.error("Error procesando posición directa: {}", e.getMessage());
                return;
            }
        }

        // Modo avanzado: usar GameLogicService con direction y gameId
        String direction = (String) payload.get("direction");
        if (direction == null) {
            log.warn("Ni posición ni dirección proporcionada en move para {}", username);
            return;
        }

        String gameIdStr = (String) payload.get("gameId");
        if (gameIdStr == null) {
             log.error("Falta gameId en el payload de movimiento con dirección");
             return;
        }
        
        UUID gameId = UUID.fromString(gameIdStr);

        try {
            PlayerMoveRequestDTO moveRequest = new PlayerMoveRequestDTO(username, direction);
            
            GameState updatedState = gameLogicService.movePlayer(gameId, moveRequest);

            messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX + lobbyCode + MOVE_SUFFIX, updatedState);
            
        } catch (Exception e) {
            log.error("Error procesando movimiento: {}", e.getMessage());
        }
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
        messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX+ lobbyCode + MOVE_SUFFIX, finishEvent);

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
        messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX + lobbyCode + MOVE_SUFFIX, event);

        log.info("Jugador {} salió del juego {}", username, lobbyCode);
    }

    /**
     * Envía sincronización completa del estado del juego
     */
    private void sendSync(String lobbyCode) {
        List<PlayerGameStateDTO> players = sessionManager.getPlayers(lobbyCode);
        if (!players.isEmpty()) {
            GameSyncDTO syncData = new GameSyncDTO(players);
            messagingTemplate.convertAndSend(TOPIC_GAME_PREFIX + lobbyCode + SYNC_SUFFIX, syncData);
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
