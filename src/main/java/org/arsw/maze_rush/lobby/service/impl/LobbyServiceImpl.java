package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Implementación del servicio {@link LobbyService} que gestiona la lógica de negocio
 * relacionada con los lobbies o salas de juego.
 *
 * <p>Incluye operaciones de creación, consulta y eliminación de lobbies,
 * así como la generación de códigos únicos y validación de parámetros.</p>
 *
 * <h3>Características principales:</h3>
 * <ul>
 *   <li>Generación automática de códigos alfanuméricos únicos de 6 caracteres.</li>
 *   <li>Validación del número máximo de jugadores permitido (entre 2 y 4).</li>
 *   <li>Operaciones CRUD con validaciones de existencia.</li>
 * </ul>
 */
@Service
public class LobbyServiceImpl implements LobbyService {

    private final LobbyRepository lobbyRepository;

    /**
     * Constructor principal del servicio.
     *
     * @param lobbyRepository repositorio JPA para el manejo de datos de lobbies
     */
    public LobbyServiceImpl(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    /**
     * Genera un código aleatorio de 6 caracteres alfanuméricos
     * para identificar de forma única cada lobby.
     *
     * @return código único generado
     */
    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * Crea un nuevo lobby con los parámetros especificados, validando
     * el rango de jugadores y generando un código único.
     *
     * @param mazeSize tamaño del laberinto (Pequeño, Mediano o Grande)
     * @param maxPlayers número máximo de jugadores (2 a 4)
     * @param visibility visibilidad del lobby (Pública o Privada)
     * @param creatorUsername nombre del usuario que crea el lobby
     * @return la entidad {@link LobbyEntity} creada y guardada
     * @throws IllegalArgumentException si el número de jugadores es inválido
     */
    @Override
    public LobbyEntity createLobby(String mazeSize, int maxPlayers, String visibility, String creatorUsername) {
        if (maxPlayers < 2 || maxPlayers > 4) {
            throw new IllegalArgumentException("El número de jugadores debe estar entre 2 y 4");
        }

        String code = generateCode();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setMazeSize(mazeSize);
        lobby.setMaxPlayers(maxPlayers);
        lobby.setVisibility(visibility);
        lobby.setCreatorUsername(creatorUsername);

        return lobbyRepository.save(lobby);
    }

    /**
     * Obtiene todos los lobbies registrados en el sistema.
     *
     * @return lista de entidades {@link LobbyEntity}
     */
    @Override
    public List<LobbyEntity> getAllLobbies() {
        return lobbyRepository.findAll();
    }

    /**
     * Busca un lobby por su código único.
     *
     * @param code código de 6 caracteres del lobby
     * @return entidad {@link LobbyEntity} correspondiente al código
     * @throws IllegalArgumentException si no se encuentra el lobby
     */
    @Override
    public LobbyEntity getLobbyByCode(String code) {
        Optional<LobbyEntity> lobbyOpt = lobbyRepository.findByCode(code);
        if (lobbyOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el lobby con el código: " + code);
        }
        return lobbyOpt.get();
    }

    /**
     * Elimina un lobby existente mediante su identificador UUID.
     *
     * @param id identificador único del lobby
     * @throws IllegalArgumentException si no se encuentra el lobby
     */
    @Override
    public void deleteLobby(UUID id) {
        if (!lobbyRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el lobby con el ID: " + id);
        }
        lobbyRepository.deleteById(id);
    }
}
