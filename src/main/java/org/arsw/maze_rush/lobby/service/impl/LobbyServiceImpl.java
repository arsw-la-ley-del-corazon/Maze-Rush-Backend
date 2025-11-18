package org.arsw.maze_rush.lobby.service.impl;

import org.arsw.maze_rush.common.exceptions.LobbyFullException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.dto.LobbyCacheDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class LobbyServiceImpl implements LobbyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOBBY_NOT_FOUND = "No se encontró el lobby con el código: ";
    private static final String USER_NOT_FOUND = "Usuario no encontrado: ";
    private static final String REDIS_LOBBY_PREFIX = "lobby:";

    private final UserRepository userRepository;
    private final LobbyRepository lobbyRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public LobbyServiceImpl(LobbyRepository lobbyRepository,
                            UserRepository userRepository,
                            RedisTemplate<String, Object> redisTemplate) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }


    private LobbyCacheDTO toCache(LobbyEntity lobby) {
        LobbyCacheDTO cache = new LobbyCacheDTO();
        cache.setId(lobby.getId());
        cache.setCode(lobby.getCode());
        cache.setMazeSize(lobby.getMazeSize());
        cache.setMaxPlayers(lobby.getMaxPlayers());
        cache.setPublic(lobby.isPublic());
        cache.setStatus(lobby.getStatus());
        cache.setCreatorUsername(lobby.getCreatorUsername());
        cache.setPlayers(
                lobby.getPlayers().stream()
                        .map(UserEntity::getUsername)
                        .toList()
        );
        return cache;
    }

    private LobbyEntity fromCache(LobbyCacheDTO cache) {
        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(cache.getId());
        lobby.setCode(cache.getCode());
        lobby.setMazeSize(cache.getMazeSize());
        lobby.setMaxPlayers(cache.getMaxPlayers());
        lobby.setPublic(cache.isPublic());
        lobby.setStatus(cache.getStatus());
        lobby.setCreatorUsername(cache.getCreatorUsername());
        lobby.setPlayers(
                cache.getPlayers().stream()
                        .map(username -> userRepository.findByUsernameIgnoreCase(username).orElse(null))
                        .filter(u -> u != null)
                        .collect(Collectors.toSet())
        );
        return lobby;
    }

    private void saveToRedis(LobbyEntity lobby) {
        redisTemplate.opsForValue().set(
                REDIS_LOBBY_PREFIX + lobby.getCode(),
                toCache(lobby),
                1,
                TimeUnit.HOURS
        );
    }

    private String generateCode() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (lobbyRepository.findByCode(code).isPresent());
        return code;
    }


    @Override
    public LobbyEntity createLobby(String mazeSize, int maxPlayers, boolean isPublic,
                                   String status, String creatorUsername) {

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

        UserEntity creator = userRepository.findByUsernameIgnoreCase(creatorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado: " + creatorUsername));

        lobby.addPlayer(creator);
        creator.getLobbies().add(lobby);

        LobbyEntity savedLobby = lobbyRepository.save(lobby);
        saveToRedis(savedLobby);

        return savedLobby;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LobbyEntity> getAllLobbies() {
        return lobbyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public LobbyEntity getLobbyByCode(String code) {

        LobbyCacheDTO cachedLobby =
                (LobbyCacheDTO) redisTemplate.opsForValue().get(REDIS_LOBBY_PREFIX + code);

        if (cachedLobby != null) {
            return fromCache(cachedLobby);
        }

        return lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));
    }

    @Override
    public LobbyEntity joinLobbyByCode(String code, String username) {

        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));

        if (lobby.getPlayers().contains(user)) {
            throw new IllegalStateException("El jugador ya está en este lobby");
        }

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            throw new LobbyFullException("El lobby ya alcanzó el número máximo de jugadores permitidos");
        }

        lobby.addPlayer(user);
        LobbyEntity updatedLobby = lobbyRepository.save(lobby);
        saveToRedis(updatedLobby);

        return updatedLobby;
    }

    @Override
    public void leaveLobby(String code, String username) {

        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + username));

        if (!lobby.getPlayers().contains(user)) {
            throw new IllegalStateException("El jugador no pertenece a este lobby");
        }

        lobby.removePlayer(user);

        if (lobby.getPlayers().isEmpty()) {
            lobby.setStatus("ABANDONADO");
        }

        LobbyEntity updatedLobby = lobbyRepository.save(lobby);
        saveToRedis(updatedLobby);
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
