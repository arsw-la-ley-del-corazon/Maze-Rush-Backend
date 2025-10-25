package org.arsw.maze_rush.lobby.service.impl;

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

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class LobbyServiceImpl implements LobbyService {

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

    /** Genera un código que no exista en DB. */
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

        // 🔹 Buscar y agregar al creador
        UserEntity creator = userRepository.findByUsernameIgnoreCase(creatorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado: " + creatorUsername));

        lobby.addPlayer(creator);     
        creator.getLobbies().add(lobby);

        LobbyEntity savedLobby = lobbyRepository.save(lobby);

        LobbyCacheDTO cache = new LobbyCacheDTO();
        cache.setId(savedLobby.getId());
        cache.setCode(savedLobby.getCode());
        cache.setMazeSize(savedLobby.getMazeSize());
        cache.setMaxPlayers(savedLobby.getMaxPlayers());
        cache.setPublic(savedLobby.isPublic());
        cache.setStatus(savedLobby.getStatus());
        cache.setCreatorUsername(savedLobby.getCreatorUsername());
        cache.setPlayers(savedLobby.getPlayers()
                .stream()
                .map(UserEntity::getUsername)
                .toList());

        redisTemplate.opsForValue().set("lobby:" + savedLobby.getCode(), cache, 1, TimeUnit.HOURS);

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
        //  Primero buscar en Redis (DTO)
        LobbyCacheDTO cachedLobby = (LobbyCacheDTO) redisTemplate.opsForValue().get("lobby:" + code);
        if (cachedLobby != null) {
        LobbyEntity lobby = new LobbyEntity();
        lobby.setId(cachedLobby.getId());
        lobby.setCode(cachedLobby.getCode());
        lobby.setMazeSize(cachedLobby.getMazeSize());
        lobby.setMaxPlayers(cachedLobby.getMaxPlayers());
        lobby.setPublic(cachedLobby.isPublic());
        lobby.setStatus(cachedLobby.getStatus());
        lobby.setCreatorUsername(cachedLobby.getCreatorUsername());
        lobby.setPlayers(
            cachedLobby.getPlayers().stream()
                .map(username -> userRepository.findByUsernameIgnoreCase(username).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toSet())
        );
        return lobby;
    }

        // Si no está en Redis, buscar en base de datos
        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("No se encontró el lobby con el código: " + code));

        return lobby;
    }

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
        LobbyEntity updatedLobby = lobbyRepository.save(lobby);

        
        LobbyCacheDTO cache = new LobbyCacheDTO();
        cache.setId(updatedLobby.getId());
        cache.setCode(updatedLobby.getCode());
        cache.setMazeSize(updatedLobby.getMazeSize());
        cache.setMaxPlayers(updatedLobby.getMaxPlayers());
        cache.setPublic(updatedLobby.isPublic());
        cache.setStatus(updatedLobby.getStatus());
        cache.setCreatorUsername(updatedLobby.getCreatorUsername());
        cache.setPlayers(updatedLobby.getPlayers()
                .stream()
                .map(UserEntity::getUsername)
                .toList());

        redisTemplate.opsForValue().set("lobby:" + updatedLobby.getCode(), cache, 1, TimeUnit.HOURS);
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
