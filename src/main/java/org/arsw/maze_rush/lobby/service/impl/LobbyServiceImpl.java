package org.arsw.maze_rush.lobby.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.common.exceptions.LobbyFullException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.dto.LobbyCacheDTO;
import org.arsw.maze_rush.lobby.dto.LobbyUpdateEventDTO;
import org.arsw.maze_rush.lobby.dto.PlayerEventDTO;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.lobby.repository.LobbyPlayerRepository;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class LobbyServiceImpl implements LobbyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOBBY_NOT_FOUND = "No se encontró el lobby con el código: ";
    private static final String USER_NOT_FOUND = "Usuario no encontrado: ";
    private static final String REDIS_LOBBY_PREFIX = "lobby:";
    private static final String LOBBY_TOPIC = "/topic/lobby/";



    private final UserRepository userRepository;
    private final LobbyRepository lobbyRepository;
    private final LobbyPlayerRepository lobbyPlayerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final org.arsw.maze_rush.maze.service.MazeService mazeService;
    private final org.arsw.maze_rush.game.service.GameSessionManager gameSessionManager;

    public LobbyServiceImpl(LobbyRepository lobbyRepository,
                            UserRepository userRepository,
                            LobbyPlayerRepository lobbyPlayerRepository,
                            RedisTemplate<String, Object> redisTemplate,
                            SimpMessagingTemplate messagingTemplate,
                            org.arsw.maze_rush.maze.service.MazeService mazeService,
                            org.arsw.maze_rush.game.service.GameSessionManager gameSessionManager) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.lobbyPlayerRepository = lobbyPlayerRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.mazeService = mazeService;
        this.gameSessionManager = gameSessionManager;
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
        // Los jugadores se cargarán desde la base de datos cuando sea necesario
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

    private void deleteFromRedis(String code) {
        redisTemplate.delete(REDIS_LOBBY_PREFIX + code);
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

        LobbyEntity savedLobby = lobbyRepository.save(lobby);
        
        // Agregar el creador como jugador usando LobbyPlayerEntity
        lobby.addPlayer(creator);
        lobbyRepository.save(savedLobby);

        saveToRedis(savedLobby);
        
        // Notificar creación de sala pública
        sendGlobalLobbyUpdate(savedLobby, "created");

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

        if (lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)) {
            throw new IllegalStateException("El jugador ya está en este lobby");
        }

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            throw new LobbyFullException("El lobby ya alcanzó el número máximo de jugadores permitidos");
        }

        lobby.addPlayer(user);
        LobbyEntity updatedLobby = lobbyRepository.save(lobby);
        saveToRedis(updatedLobby);

        // Enviar evento de jugador unido
        sendPlayerEvent(code, username, "joined", updatedLobby);
        
        // Enviar actualización global si el lobby es público
        if (updatedLobby.isPublic()) {
            sendGlobalLobbyUpdate(updatedLobby, "updated");
        }

        return updatedLobby;
    }

    @Override
    public void leaveLobby(String code, String username) {

        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));

        // Verificar si el jugador está en el lobby antes de intentar removerlo
        if (!lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)) {
            log.warn("El jugador {} no pertenece al lobby {}, ignorando operación", username, code);
            return; // No lanzar excepción, simplemente retornar
        }

        lobby.removePlayer(user);

        // Si el lobby quedó vacío, eliminarlo directamente
        if (lobby.getPlayers().isEmpty()) {
            log.info("Lobby {} quedó vacío, eliminándolo...", code);
            
            // Notificar eliminación global si era público
            if (lobby.isPublic()) {
                sendGlobalLobbyUpdate(lobby, "deleted");
            }
            
            lobbyRepository.delete(lobby);
            deleteFromRedis(code);
            
            // Enviar evento de lobby eliminado
            messagingTemplate.convertAndSend(LOBBY_TOPIC + code, 
                Map.of("type", "lobby_deleted", "code", code));
            return;
        }

        LobbyEntity updatedLobby = lobbyRepository.save(lobby);
        saveToRedis(updatedLobby);

        // Enviar evento de jugador salido
        sendPlayerEvent(code, username, "left", updatedLobby);
    }

    @Override
    public void removePlayerFromLobby(UUID lobbyId, UUID userId) {

        LobbyEntity lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby no encontrado con ID: " + lobbyId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        lobby.removePlayer(user);
        
        // Si el lobby quedó vacío, eliminarlo
        if (lobby.getPlayers().isEmpty()) {
            lobbyRepository.delete(lobby);
            deleteFromRedis(lobby.getCode());
        } else {
            lobbyRepository.save(lobby);
            saveToRedis(lobby);
        }
    }
    
    @Override
    public void sendChatMessage(String code, String username, String message) {
        // Validar que el lobby existe
        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));
        
        // Validar que el usuario pertenece al lobby
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));
        
        if (!lobby.getPlayers().contains(user)) {
            throw new IllegalStateException("El jugador no pertenece a este lobby");
        }
        
        // El mensaje se envía a través del WebSocket, aquí solo validamos
    }
    
    @Override
    public boolean toggleReady(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));
        
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));
        
        if (!lobby.getPlayers().contains(user)) {
            throw new IllegalStateException("El jugador no pertenece a este lobby");
        }
        
        // Almacenar estado "ready" en Redis
        String redisKey = REDIS_LOBBY_PREFIX + code + ":ready:" + username;
        Boolean currentState = (Boolean) redisTemplate.opsForValue().get(redisKey);
        boolean newState = currentState == null || !currentState;
        
        redisTemplate.opsForValue().set(redisKey, newState, 1, TimeUnit.HOURS);
        
        log.info("{} cambió su estado a: {}", username, newState ? "Listo" : "No listo");
        
        return newState;
    }
    
    @Override
    public void startGame(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));
        
        // Validar que el usuario es el creador del lobby
        if (!lobby.getCreatorUsername().equals(username)) {
            throw new IllegalStateException("Solo el creador del lobby puede iniciar el juego");
        }
        
        // Validar que hay suficientes jugadores
        if (lobby.getPlayers().size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores para iniciar el juego");
        }
        
        // Cambiar el estado del lobby a "EN_CURSO"
        lobby.setStatus("EN_CURSO");
        LobbyEntity updatedLobby = lobbyRepository.save(lobby);
        saveToRedis(updatedLobby);
        
        // Generar el laberinto una sola vez
        org.arsw.maze_rush.maze.entities.MazeEntity maze = mazeService.generateMaze(lobby.getMazeSize());
        
        // Almacenar el ID del laberinto en la sesión
        gameSessionManager.setMaze(code, maze.getId().toString());
        
        // Notificar a todos los jugadores que el juego ha comenzado con el laberinto compartido
        try {
            Map<String, Object> gameStartEvent = new HashMap<>();
            gameStartEvent.put("action", "game_started");
            gameStartEvent.put("lobbyCode", code);
            gameStartEvent.put("mazeSize", lobby.getMazeSize());
            gameStartEvent.put("maze", maze); // Enviar el laberinto completo
            gameStartEvent.put("players", lobby.getPlayers().stream()
                    .map(UserEntity::getUsername)
                    .collect(Collectors.toList()));
            
            messagingTemplate.convertAndSend(LOBBY_TOPIC + code + "/game", gameStartEvent);
            log.info("Juego iniciado en lobby {} con {} jugadores y laberinto {}", 
                code, lobby.getPlayers().size(), maze.getId());
        } catch (Exception e) {
            log.error("Error al notificar inicio de juego para lobby {}: {}", code, e.getMessage());
        }
    }

    @Override
    public void notifyPlayersUpdate(String code) {
        try {
            LobbyEntity lobby = lobbyRepository.findByCode(code)
                    .orElseThrow(() -> new NotFoundException(LOBBY_NOT_FOUND + code));

            List<String> playerUsernames = lobby.getPlayers().stream()
                    .map(UserEntity::getUsername)
                    .collect(Collectors.toList());

            PlayerEventDTO event = new PlayerEventDTO();
            event.setUsername("system");
            event.setAction("update");
            event.setPlayers(playerUsernames);
            event.setPlayerCount(playerUsernames.size());
            event.setMaxPlayers(lobby.getMaxPlayers());

            messagingTemplate.convertAndSend(LOBBY_TOPIC + code + "/players", event);
            log.info("Notificación de actualización de jugadores enviada para lobby {}", code);
        } catch (Exception e) {
            log.error("Error al notificar actualización de jugadores para lobby {}: {}", code, e.getMessage());
        }
    }

    /**
     * Envía un evento WebSocket cuando un jugador se une o sale del lobby
     */
    private void sendPlayerEvent(String code, String username, String action, LobbyEntity lobby) {
        try {
            List<String> playerUsernames = lobby.getPlayers().stream()
                    .map(UserEntity::getUsername)
                    .collect(Collectors.toList());

            PlayerEventDTO event = new PlayerEventDTO();
            event.setUsername(username);
            event.setAction(action);
            event.setPlayers(playerUsernames);
            event.setPlayerCount(playerUsernames.size());
            event.setMaxPlayers(lobby.getMaxPlayers());

            messagingTemplate.convertAndSend(LOBBY_TOPIC + code + "/players", event);
        } catch (Exception e) {
            // Log error pero no fallar la operación principal
            log.error("Error al enviar evento de jugador para lobby {}: {}", code, e.getMessage());
        }
    }
    
    /**
     * Envía una actualización global de un lobby público a /topic/lobby/updates
     * Solo se envía para lobbies públicos en espera
     */
    private void sendGlobalLobbyUpdate(LobbyEntity lobby, String action) {
        try {
            // Solo enviar si el lobby es público y está en espera
            if (!lobby.isPublic() || !"EN_ESPERA".equals(lobby.getStatus())) {
                return;
            }
            
            LobbyUpdateEventDTO update = new LobbyUpdateEventDTO();
            update.setCode(lobby.getCode());
            update.setPlayerCount(lobby.getPlayers().size());
            update.setStatus(lobby.getStatus());
            update.setAction(action);
            
            messagingTemplate.convertAndSend("/topic/lobby/updates", update);
            log.info("Actualización global enviada para lobby público {}: action={}, players={}", 
                    lobby.getCode(), action, lobby.getPlayers().size());
        } catch (Exception e) {
            log.error("Error al enviar actualización global para lobby {}: {}", lobby.getCode(), e.getMessage());
        }
    }
}
