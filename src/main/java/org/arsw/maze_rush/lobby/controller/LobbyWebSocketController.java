package org.arsw.maze_rush.lobby.controller;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.lobby.dto.ChatMessageDTO;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Controlador WebSocket para eventos en tiempo real del lobby
 * Maneja chat, estado de jugadores y eventos de inicio de partida
 */
@Controller
@Slf4j
public class LobbyWebSocketController {

    private final LobbyService lobbyService;

    public LobbyWebSocketController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    /**
     * Maneja mensajes de chat en el lobby
     * Los clientes envían a: /app/lobby/{code}/chat
     * Respuesta broadcast a: /topic/lobby/{code}/chat
     */
    @MessageMapping("/lobby/{code}/chat")
    @SendTo("/topic/lobby/{code}/chat")
    public ChatMessageDTO sendMessage(
            @DestinationVariable String code,
            @Payload Map<String, String> payload,
            Principal principal) {
        
        String message = payload.get("message");
        String username = principal.getName();
        
        log.debug("Mensaje de chat en lobby {}: {} - {}", code, username, message);
        
        // El servicio maneja la validación y broadcast
        lobbyService.sendChatMessage(code, username, message);
        
        return new ChatMessageDTO(username, message);
    }

    /**
     * Maneja el cambio de estado "listo" de un jugador
     * Los clientes envían a: /app/lobby/{code}/ready
     * Respuesta broadcast a: /topic/lobby/{code}/ready
     */
    @MessageMapping("/lobby/{code}/ready")
    public void toggleReady(
            @DestinationVariable String code,
            Principal principal) {
        
        String username = principal.getName();
        log.info("Toggling ready para {} en lobby {}", username, code);
        
        lobbyService.toggleReady(code, username);
    }

    /**
     * Maneja el inicio del juego por parte del host
     * Los clientes envían a: /app/lobby/{code}/start
     * Respuesta broadcast a: /topic/lobby/{code}/game
     */
    @MessageMapping("/lobby/{code}/start")
    public void startGame(
            @DestinationVariable String code,
            Principal principal) {
        
        String username = principal.getName();
        log.info("Iniciando juego en lobby {} por {}", code, username);
        
        lobbyService.startGame(code, username);
    }

    /**
     * Maneja cuando un jugador se conecta al WebSocket del lobby
     */
    @MessageMapping("/lobby/{code}/connect")
    public void handleConnect(
            @DestinationVariable String code,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String username = principal.getName();
        log.info("Usuario {} conectado a WebSocket del lobby {}", username, code);
        
        // Guardar información en la sesión WebSocket
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("lobbyCode", code);
    }
}
