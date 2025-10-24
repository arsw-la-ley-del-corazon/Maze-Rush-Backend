package org.arsw.maze_rush.lobby.service;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;

import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de los lobbies o salas de juego.
 *
 * <p>Define las operaciones principales para la creación, consulta y eliminación
 * de lobbies dentro del sistema Maze Rush. Su implementación se encuentra en
 * {@link org.arsw.maze_rush.lobby.service.impl.LobbyServiceImpl}.</p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Crear nuevos lobbies con validaciones de jugadores y visibilidad.</li>
 *   <li>Listar todos los lobbies registrados en el sistema.</li>
 *   <li>Buscar lobbies por su código único de seis caracteres.</li>
 *   <li>Eliminar lobbies existentes a partir de su identificador UUID.</li>
 * </ul>
 *
 * <h3>Reglas de negocio:</h3>
 * <ul>
 *   <li>El número máximo de jugadores debe estar entre 2 y 4.</li>
 *   <li>Los códigos de lobby son únicos y generados automáticamente.</li>
 * </ul>
 */
public interface LobbyService {

    /**
     * Crea un nuevo lobby con los parámetros especificados.
     *
     * @param mazeSize tamaño del laberinto (Pequeño, Mediano o Grande)
     * @param maxPlayers número máximo de jugadores (de 2 a 4)
     * @param isPublic indica si el lobby es público (true) o privado (false)
     * @param status estado inicial del lobby (si es null o vacío, se usará el valor por defecto)
     * @param creatorUsername nombre del usuario que crea la sala
     * @return entidad {@link LobbyEntity} persistida en la base de datos
     * @throws IllegalArgumentException si el número de jugadores es inválido
     */
    LobbyEntity createLobby(String mazeSize, int maxPlayers, boolean isPublic, String status, String creatorUsername);

    /**
     * Obtiene la lista de todos los lobbies registrados en el sistema.
     *
     * @return lista de entidades {@link LobbyEntity}
     */
    List<LobbyEntity> getAllLobbies();

    /**
     * Busca un lobby existente por su código único.
     *
     * @param code código de 6 caracteres asociado al lobby
     * @return entidad {@link LobbyEntity} correspondiente al código
     * @throws IllegalArgumentException si no se encuentra el lobby con ese código
     */
    LobbyEntity getLobbyByCode(String code);

    /**
     * Elimina un lobby existente mediante su identificador único (UUID).
     *
     * @param id identificador único del lobby a eliminar
     * @throws IllegalArgumentException si no se encuentra el lobby
     */
    void deleteLobby(UUID id);

    void addPlayerToLobby(UUID lobbyId, UUID userId);
    void removePlayerFromLobby(UUID lobbyId, UUID userId);

}
