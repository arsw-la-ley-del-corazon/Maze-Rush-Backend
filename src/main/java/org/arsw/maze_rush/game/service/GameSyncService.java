package org.arsw.maze_rush.game.service;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.game.dto.GameSyncDTO;
import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Servicio para sincronización periódica del estado del juego
 * Envía actualizaciones completas del estado cada cierto intervalo
 * para mantener la consistencia entre clientes
 */
@Service
@Slf4j
public class GameSyncService {

    private final GameSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    public GameSyncService(GameSessionManager sessionManager, 
                          SimpMessagingTemplate messagingTemplate) {
        this.sessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sincronización periódica cada 5 segundos
     * Envía el estado completo de todos los jugadores en cada sesión activa
     */
    @Scheduled(fixedRate = 5000)
    public void syncGameStates() {
        Set<String> activeSessions = sessionManager.getActiveSessions();
        
        if (activeSessions.isEmpty()) {
            return;
        }

        log.debug("Sincronizando {} sesiones de juego activas", activeSessions.size());

        for (String lobbyCode : activeSessions) {
            try {
                syncSession(lobbyCode);
            } catch (Exception e) {
                log.error("Error sincronizando sesión {}: {}", lobbyCode, e.getMessage(), e);
            }
        }
    }

    /**
     * Sincroniza una sesión específica
     */
    private void syncSession(String lobbyCode) {
        List<PlayerGameStateDTO> players = sessionManager.getPlayers(lobbyCode);
        
        if (players.isEmpty()) {
            log.debug("Sesión {} sin jugadores, omitiendo sincronización", lobbyCode);
            return;
        }

        // Crear y enviar mensaje de sincronización
        GameSyncDTO syncData = new GameSyncDTO(players);
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/sync", syncData);
        
        log.trace("Sincronización enviada para {} con {} jugadores", 
            lobbyCode, players.size());
    }

    /**
     * Fuerza una sincronización inmediata para un lobby específico
     * Útil para sincronizar después de eventos importantes
     */
    public void forceSyncSession(String lobbyCode) {
        log.debug("Forzando sincronización para lobby {}", lobbyCode);
        syncSession(lobbyCode);
    }

    /**
     * Limpia sesiones inactivas (sin jugadores durante mucho tiempo)
     * Se ejecuta cada minuto
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupInactiveSessions() {
        Set<String> activeSessions = sessionManager.getActiveSessions();
        
        for (String lobbyCode : activeSessions) {
            int playerCount = sessionManager.getPlayerCount(lobbyCode);
            
            // Si no hay jugadores, limpiar la sesión
            if (playerCount == 0) {
                log.info("Limpiando sesión inactiva: {}", lobbyCode);
                sessionManager.clearSession(lobbyCode);
            }
        }
    }
}
