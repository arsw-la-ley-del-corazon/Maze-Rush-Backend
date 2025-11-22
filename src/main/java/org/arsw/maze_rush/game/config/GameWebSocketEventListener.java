package org.arsw.maze_rush.game.config;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.game.dto.GameEventDTO;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listener para eventos de desconexión WebSocket
 * Limpia el estado del juego cuando un jugador se desconecta inesperadamente
 */
@Component
@Slf4j
public class GameWebSocketEventListener {

    private final GameSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketEventListener(GameSessionManager sessionManager,
                                     SimpMessagingTemplate messagingTemplate) {
        this.sessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Maneja desconexiones de WebSocket
     * Limpia el estado del jugador y notifica a los demás
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Obtener información de la sesión
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String lobbyCode = (String) headerAccessor.getSessionAttributes().get("lobbyCode");

        if (username != null && lobbyCode != null) {
            // Verificar si el jugador está en una sesión de juego
            if (sessionManager.sessionExists(lobbyCode)) {
                log.info("Jugador {} desconectado del juego {} (sesión: {})", 
                    username, lobbyCode, headerAccessor.getSessionId());

                // Remover jugador de la sesión
                sessionManager.removePlayer(lobbyCode, username);

                // Notificar a los demás jugadores
                GameEventDTO leaveEvent = new GameEventDTO("player_left", username);
                messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/move", leaveEvent);
                
                log.info("Estado limpiado para jugador {} en juego {}", username, lobbyCode);
            }
        }
    }
}
