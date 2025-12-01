package org.arsw.maze_rush.lobby.entities;

/**
 * Estados posibles de un lobby
 */
public enum LobbyStatus {
    WAITING,    // Esperando jugadores
    STARTING,   // Iniciando partida
    IN_GAME,    // Partida en curso
    FINISHED    // Partida terminada
}
