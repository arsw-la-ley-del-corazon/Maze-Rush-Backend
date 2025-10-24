package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Implementación del servicio {@link LobbyService} que gestiona la lógica de negocio
 * de los lobbies o salas de juego.
 */
@Service
@Transactional
public class LobbyServiceImpl implements LobbyService {

    private final UserRepository userRepository;

    private final LobbyRepository lobbyRepository;

    public LobbyServiceImpl(LobbyRepository lobbyRepository, UserRepository userRepository) {

        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
    }

    /** Genera un código aleatorio de 6 caracteres alfanuméricos. */
    private String generateCode() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /** Genera un código que no exista ya en BD. */
    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (lobbyRepository.findByCode(code).isPresent());
        return code;
    }

    @Override
    public LobbyEntity createLobby(String mazeSize, int maxPlayers, boolean isPublic, String status, String creatorUsername) {
        if (maxPlayers < 2 || maxPlayers > 4) {
            throw new IllegalArgumentException("El número de jugadores debe estar entre 2 y 4");
        }
        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(generateUniqueCode());
        lobby.setMazeSize(mazeSize);
        lobby.setMaxPlayers(maxPlayers);
        lobby.setPublic(isPublic);
        lobby.setStatus((status == null || status.isBlank()) ? "EN_ESPERA" : status);
        lobby.setCreatorUsername(creatorUsername);

        return lobbyRepository.save(lobby);
    }

    /**
     * Obtiene todos los lobbies registrados en el sistema.
     *
     * @return lista de entidades {@link LobbyEntity}
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public LobbyEntity getLobbyByCode(String code) {
        return lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("No se encontró el lobby con el código: " + code));
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
            throw new NotFoundException("No se encontró el lobby con el ID: " + id);
        }
        lobbyRepository.deleteById(id);
    }

    @Override
    public void addPlayerToLobby(UUID lobbyId, UUID userId) {
        LobbyEntity lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby no encontrado con ID: " + lobbyId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            throw new IllegalStateException("El lobby ya alcanzó el número máximo de jugadores");
        }

        if (lobby.getPlayers().contains(user)) {
            throw new IllegalStateException("El jugador ya está en este lobby");
        }


        lobby.addPlayer(user);
        lobbyRepository.save(lobby);
    }

    @Override
    public void removePlayerFromLobby(UUID lobbyId, UUID userId) {
        LobbyEntity lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby no encontrado con ID: " + lobbyId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        lobby.removePlayer(user);
        lobbyRepository.save(lobby);
    }




}
