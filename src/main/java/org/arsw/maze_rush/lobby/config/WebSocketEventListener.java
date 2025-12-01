package org.arsw.maze_rush.lobby.config;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;

/**
 * Listener de eventos WebSocket para manejar conexiones y desconexiones
 * Automáticamente remueve jugadores del lobby cuando se desconectan
 */
@Component
@Slf4j
public class WebSocketEventListener {

    private final LobbyService lobbyService;

    public WebSocketEventListener(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Nueva conexión WebSocket: {}", headerAccessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("Desconexión WebSocket detectada: {}", sessionId);

        // Obtener información de la sesión
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            String lobbyCode = (String) sessionAttributes.get("lobbyCode");

            if (username != null && lobbyCode != null) {
                try {
                    log.info("Removiendo jugador {} del lobby {} debido a desconexión", username, lobbyCode);
                    lobbyService.leaveLobby(lobbyCode, username);
                    log.info("Jugador {} removido exitosamente del lobby {}", username, lobbyCode);
                } catch (Exception e) {
                    log.error("Error al remover jugador {} del lobby {}: {}", username, lobbyCode, e.getMessage());
                }
            }
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Principal principal = headerAccessor.getUser();
        
        if (destination != null && principal != null) {
            log.debug("Suscripción a {} por usuario {}", destination, principal.getName());
            
            // Si se suscribe a un topic de lobby, guardar información en la sesión
            if (destination != null && destination.startsWith("/topic/lobby/")) {
                String lobbyCode = extractLobbyCodeFromDestination(destination);
                if (lobbyCode != null) {
                    Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
                    if (sessionAttributes != null) {
                        sessionAttributes.put("username", principal.getName());
                        sessionAttributes.put("lobbyCode", lobbyCode);
                        log.debug("Información de lobby guardada en sesión: {} -> {}", principal.getName(), lobbyCode);
                    }
                }
            }
        }
    }

    String extractLobbyCodeFromDestination(String destination) {
        // Formato esperado: /topic/lobby/{code}/...
        if (destination == null || !destination.startsWith("/topic/lobby/")) {
            return null;
        }
        
        String[] parts = destination.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }
}

