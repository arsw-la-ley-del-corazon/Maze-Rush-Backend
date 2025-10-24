package org.arsw.maze_rush.lobby.service;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de lobbies de juego.
 * 
 * Provee operaciones CRUD básicas:
 * <ul>
 *   <li>Crear un nuevo lobby</li>
 *   <li>Listar todos los lobbies existentes</li>
 *   <li>Obtener un lobby por su código único</li>
 *   <li>Eliminar un lobby por su identificador UUID</li>
 * </ul>
 *
 * @author
 *   Jeisson (Equipo Maze Rush)
 */
public interface LobbyService {

    /**
     * Crea un nuevo lobby con los parámetros especificados.
     *
     * @param mazeSize tamaño del laberinto (Pequeño, Mediano, Grande)
     * @param maxPlayers número máximo de jugadores (2–4)
     * @param visibility visibilidad del lobby (Pública o Privada)
     * @param creatorUsername nombre del usuario que crea la sala
     * @return {@link LobbyEntity} entidad persistida en la base de datos
     */
    LobbyEntity createLobby(String mazeSize, int maxPlayers, String visibility, String creatorUsername);

    /**
     * Obtiene la lista de todos los lobbies existentes.
     *
     * @return lista de entidades {@link LobbyEntity}
     */
    List<LobbyEntity> getAllLobbies();

    /**
     * Busca un lobby por su código único.
     *
     * @param code código de 6 caracteres del lobby
     * @return {@link LobbyEntity} correspondiente al código
     * @throws IllegalArgumentException si el lobby no existe
     */
    LobbyEntity getLobbyByCode(String code);

    /**
     * Elimina un lobby usando su identificador único.
     *
     * @param id UUID del lobby a eliminar
     * @throws IllegalArgumentException si el lobby no existe
     */
    void deleteLobby(UUID id);
}
