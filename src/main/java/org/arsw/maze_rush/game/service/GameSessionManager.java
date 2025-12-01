package org.arsw.maze_rush.game.service;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar sesiones de juego en tiempo real
 * Mantiene el estado de todos los jugadores en cada partida activa
 * Thread-safe utilizando estructuras concurrentes
 */
@Service
@Slf4j
public class GameSessionManager {

    // Map: lobbyCode -> Map<username, PlayerGameStateDTO>
    private final Map<String, Map<String, PlayerGameStateDTO>> gameStates = new ConcurrentHashMap<>();
    
    // Map: lobbyCode -> gameStartTime
    private final Map<String, Instant> gameStartTimes = new ConcurrentHashMap<>();
    
    // Map: lobbyCode -> mazeId (UUID del laberinto generado)
    private final Map<String, String> gameMazes = new ConcurrentHashMap<>();

    private final Map<String, Map<String, PowerUp>> gamePowerUps = new ConcurrentHashMap<>();

    /**
     * Registra un jugador en una sesión de juego
     */
    public void addPlayer(String lobbyCode, String username) {
        gameStates.computeIfAbsent(lobbyCode, k -> new ConcurrentHashMap<>())
                .putIfAbsent(username, new PlayerGameStateDTO(username, new PositionDTO(0, 0)));
        
        // Establecer tiempo de inicio si es el primer jugador
        gameStartTimes.putIfAbsent(lobbyCode, Instant.now());
        
        log.info("Jugador {} añadido al juego {}", username, lobbyCode);
    }

    /**
     * Actualiza la posición de un jugador
     */
    public void updatePlayerPosition(String lobbyCode, String username, PositionDTO position) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        if (players != null) {
            PlayerGameStateDTO player = players.get(username);
            if (player != null) {
                player.setPosition(position);
                log.debug("Posición actualizada para {} en {}: ({}, {})", 
                    username, lobbyCode, position.getX(), position.getY());
            }
        }
    }

    /**
     * Marca a un jugador como finalizado y registra su tiempo
     */
    public void markPlayerFinished(String lobbyCode, String username) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        if (players != null) {
            PlayerGameStateDTO player = players.get(username);
            if (player != null && !player.getIsFinished()) {
                player.setIsFinished(true);
                Long finishTime = getElapsedTime(lobbyCode);
                player.setFinishTime(finishTime);
                log.info("Jugador {} terminó el juego {} en {} segundos",
                    username, lobbyCode, finishTime);
            }
        }
    }

    /**
     * Elimina un jugador de la sesión
     */
    public void removePlayer(String lobbyCode, String username) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        if (players != null) {
            players.remove(username);
            log.info("Jugador {} eliminado del juego {}", username, lobbyCode);
            
            // Si no quedan jugadores, limpiar la sesión
            if (players.isEmpty()) {
                gameStates.remove(lobbyCode);
                gameStartTimes.remove(lobbyCode);
                log.info("Sesión de juego {} eliminada (sin jugadores)", lobbyCode);
            }
        }
    }

    /**
     * Obtiene todos los jugadores de una sesión
     */
    public List<PlayerGameStateDTO> getPlayers(String lobbyCode) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        return players != null ? new ArrayList<>(players.values()) : Collections.emptyList();
    }

    /**
     * Obtiene un jugador específico
     */
    public PlayerGameStateDTO getPlayer(String lobbyCode, String username) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        return players != null ? players.get(username) : null;
    }

    /**
     * Verifica si existe una sesión de juego
     */
    public boolean sessionExists(String lobbyCode) {
        return gameStates.containsKey(lobbyCode);
    }

    /**
     * Obtiene el número de jugadores en una sesión
     */
    public int getPlayerCount(String lobbyCode) {
        Map<String, PlayerGameStateDTO> players = gameStates.get(lobbyCode);
        return players != null ? players.size() : 0;
    }

    /**
     * Obtiene todos los lobbyCodes con sesiones activas
     */
    public Set<String> getActiveSessions() {
        return new HashSet<>(gameStates.keySet());
    }

    /**
     * Limpia completamente una sesión de juego
     */
    public void clearSession(String lobbyCode) {
        gameStates.remove(lobbyCode);
        gameStartTimes.remove(lobbyCode);
        gameMazes.remove(lobbyCode);
        gamePowerUps.remove(lobbyCode);
        log.info("Sesión de juego {} limpiada manualmente", lobbyCode);
    }

    /**
     * Obtiene el tiempo de inicio del juego
     */
    public Instant getGameStartTime(String lobbyCode) {
        return gameStartTimes.get(lobbyCode);
    }

    /**
     * Calcula el tiempo transcurrido desde el inicio del juego en segundos
     */
    public Long getElapsedTime(String lobbyCode) {
        Instant startTime = gameStartTimes.get(lobbyCode);
        if (startTime != null) {
            long startMillis = startTime.toEpochMilli();
            long nowMillis = Instant.now().toEpochMilli();
            return (nowMillis - startMillis) / 1000;
        }
        return 0L;
    }
    
    /**
     * Almacena el ID del laberinto generado para esta sesión
     */
    public void setMaze(String lobbyCode, String mazeId) {
        gameMazes.put(lobbyCode, mazeId);
        log.info("Laberinto {} asignado a la sesión {}", mazeId, lobbyCode);
    }
    
    /**
     * Obtiene el ID del laberinto para esta sesión
     */
    public String getMazeId(String lobbyCode) {
        return gameMazes.get(lobbyCode);
    }
    
    /**
     * Verifica si ya se generó un laberinto para esta sesión
     */
    public boolean hasMaze(String lobbyCode) {
        return gameMazes.containsKey(lobbyCode);
    }

    /**
    * Guarda los powerups generados al inicio de la partida en el lobby especificado.
    */
    public void setPowerUps(String lobbyCode, List<PowerUp> powerUps) {
        Map<String, PowerUp> powerUpMap = new ConcurrentHashMap<>();     
        for (PowerUp p : powerUps) {
            String key = p.getX() + "," + p.getY();
            powerUpMap.put(key, p);
        }
        gamePowerUps.put(lobbyCode, powerUpMap);
        log.info("Guardados {} powerups para la sesión {}", powerUps.size(), lobbyCode);
    }

    /**
     * Verifica si hay un powerup en la posición (x, y).
     * Si existe, lo ELIMINA del mapa (recolección) y lo devuelve.
     */

    public PowerUp checkAndCollectPowerUp(String lobbyCode, int x, int y) {
        Map<String, PowerUp> powerUpMap = gamePowerUps.get(lobbyCode);
            
        if (powerUpMap != null) {
            String key = x + "," + y;
            return powerUpMap.remove(key);
        }
        return null;
    }

    /**
     * Aplica un efecto a un jugador específico en la sesión.
     */
    public void applyEffect(String lobbyCode, String username, PowerUpType type, int durationSeconds) {
        PlayerGameStateDTO player = getPlayer(lobbyCode, username);
        if (player != null) {
            long expirationTime = Instant.now().plusSeconds(durationSeconds).toEpochMilli();
            player.getActiveEffects().put(type, expirationTime);
            log.info("Efecto {} aplicado a {} por {}s", type, username, durationSeconds);
        }
    }

    /**
     * Aplica un efecto a TODOS los oponentes (excluyendo al que lo lanzó).
     */
    public void applyEffectToOpponents(String lobbyCode, String sourceUsername, PowerUpType type, int durationSeconds) {
        List<PlayerGameStateDTO> allPlayers = getPlayers(lobbyCode);
        for (PlayerGameStateDTO p : allPlayers) {
            if (!p.getUsername().equals(sourceUsername)) { 
                applyEffect(lobbyCode, p.getUsername(), type, durationSeconds);
            }
        }
    }

    /**
     * Limpia los efectos expirados de un jugador.
     */
    public void cleanExpiredEffects(String lobbyCode, String username) {
        PlayerGameStateDTO player = getPlayer(lobbyCode, username);
        if (player != null && !player.getActiveEffects().isEmpty()) {
            long now = Instant.now().toEpochMilli();
            player.getActiveEffects().values().removeIf(expiration -> now > expiration);
        }
    }




}
