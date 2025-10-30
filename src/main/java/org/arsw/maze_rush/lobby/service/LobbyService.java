package org.arsw.maze_rush.lobby.service;

import lombok.extern.slf4j.Slf4j;
import org.arsw.maze_rush.common.exceptions.BadRequestException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.lobby.dto.*;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.entities.LobbyPlayerEntity;
import org.arsw.maze_rush.lobby.entities.LobbyStatus;
import org.arsw.maze_rush.lobby.repository.LobbyPlayerRepository;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio que maneja la lógica de negocio de los lobbies
 */
@Service
@Slf4j
public class LobbyService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private final LobbyRepository lobbyRepository;
    private final LobbyPlayerRepository lobbyPlayerRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LobbyService(
            LobbyRepository lobbyRepository,
            LobbyPlayerRepository lobbyPlayerRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.lobbyRepository = lobbyRepository;
        this.lobbyPlayerRepository = lobbyPlayerRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Crea un nuevo lobby
     */
    @Transactional
    public LobbyResponseDTO createLobby(CreateLobbyRequestDTO request, String username) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Generar código único
        String code = generateUniqueCode();

        // Crear lobby
        LobbyEntity lobby = new LobbyEntity();
        lobby.setCode(code);
        lobby.setHost(user);
        lobby.setMaxPlayers(request.getMaxPlayers());
        lobby.setPrivate(request.getIsPrivate());
        lobby.setStatus(LobbyStatus.WAITING);
        lobby = lobbyRepository.save(lobby);

        // Agregar host como primer jugador
        LobbyPlayerEntity hostPlayer = new LobbyPlayerEntity();
        hostPlayer.setLobby(lobby);
        hostPlayer.setUser(user);
        hostPlayer.setHost(true);
        hostPlayer.setReady(false);
        lobbyPlayerRepository.save(hostPlayer);

        log.info("Lobby creado: {} por usuario: {}", code, username);
        return toLobbyResponseDTO(lobby);
    }

    /**
     * Permite a un jugador unirse a un lobby por código
     */
    @Transactional
    public LobbyResponseDTO joinLobby(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Validaciones
        if (lobby.isFull()) {
            throw new BadRequestException("El lobby está lleno");
        }

        if (lobby.getStatus() != LobbyStatus.WAITING) {
            throw new BadRequestException("El lobby ya ha iniciado");
        }

        if (lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)) {
            throw new BadRequestException("Ya estás en este lobby");
        }

        // Agregar jugador
        LobbyPlayerEntity player = new LobbyPlayerEntity();
        player.setLobby(lobby);
        player.setUser(user);
        player.setHost(false);
        player.setReady(false);
        lobbyPlayerRepository.save(player);

        // Notificar a todos en el lobby
        PlayerDTO playerDTO = toPlayerDTO(player);
        PlayerJoinedEventDTO event = new PlayerJoinedEventDTO(playerDTO, "joined");
        messagingTemplate.convertAndSend("/topic/lobby/" + code, event);

        log.info("Usuario {} se unió al lobby {}", username, code);
        return toLobbyResponseDTO(lobby);
    }

    /**
     * Permite a un jugador salir de un lobby
     */
    @Transactional
    public void leaveLobby(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        LobbyPlayerEntity player = lobbyPlayerRepository.findByLobbyAndUser(lobby, user)
                .orElseThrow(() -> new NotFoundException("No estás en este lobby"));

        // Si es el host, eliminar todo el lobby
        if (player.isHost()) {
            lobbyRepository.delete(lobby);
            log.info("Lobby {} eliminado por el host", code);
            messagingTemplate.convertAndSend("/topic/lobby/" + code, 
                new PlayerJoinedEventDTO(null, "lobby_closed"));
            return;
        }

        // Remover jugador
        lobbyPlayerRepository.delete(player);

        // Notificar a todos
        PlayerDTO playerDTO = toPlayerDTO(player);
        PlayerJoinedEventDTO event = new PlayerJoinedEventDTO(playerDTO, "left");
        messagingTemplate.convertAndSend("/topic/lobby/" + code, event);

        log.info("Usuario {} salió del lobby {}", username, code);
    }

    /**
     * Cambia el estado de "listo" de un jugador
     */
    @Transactional
    public void toggleReady(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        LobbyPlayerEntity player = lobbyPlayerRepository.findByLobbyAndUser(lobby, user)
                .orElseThrow(() -> new NotFoundException("No estás en este lobby"));

        // Cambiar estado
        player.setReady(!player.isReady());
        lobbyPlayerRepository.save(player);

        // Notificar a todos
        PlayerReadyEventDTO event = new PlayerReadyEventDTO(
                player.getUser().getId().toString(),
                player.getUser().getUsername(),
                player.isReady()
        );
        messagingTemplate.convertAndSend("/topic/lobby/" + code + "/ready", event);

        log.info("Usuario {} cambió estado ready a {} en lobby {}", username, player.isReady(), code);
    }

    /**
     * El host inicia el juego
     */
    @Transactional
    public void startGame(String code, String username) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        LobbyPlayerEntity player = lobbyPlayerRepository.findByLobbyAndUser(lobby, user)
                .orElseThrow(() -> new NotFoundException("No estás en este lobby"));

        // Validaciones
        if (!player.isHost()) {
            throw new BadRequestException("Solo el host puede iniciar el juego");
        }

        if (!lobby.areAllPlayersReady()) {
            throw new BadRequestException("No todos los jugadores están listos");
        }

        if (lobby.getPlayerCount() < 2) {
            throw new BadRequestException("Se necesitan al menos 2 jugadores");
        }

        // Cambiar estado del lobby
        lobby.setStatus(LobbyStatus.STARTING);
        lobbyRepository.save(lobby);

        // Notificar a todos que el juego está comenzando
        messagingTemplate.convertAndSend("/topic/lobby/" + code + "/game", "starting");

        log.info("Juego iniciado en lobby {} por {}", code, username);
    }

    /**
     * Obtiene información de un lobby
     */
    @Transactional(readOnly = true)
    public LobbyResponseDTO getLobby(String code) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));
        return toLobbyResponseDTO(lobby);
    }

    /**
     * Envía un mensaje de chat en el lobby
     */
    public void sendChatMessage(String code, String username, String message) {
        LobbyEntity lobby = lobbyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Lobby no encontrado"));

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!lobbyPlayerRepository.existsByLobbyAndUser(lobby, user)) {
            throw new BadRequestException("No estás en este lobby");
        }

        // Crear y enviar mensaje de chat
        ChatMessageDTO chatMessage = new ChatMessageDTO(username, message);
        messagingTemplate.convertAndSend("/topic/lobby/" + code + "/chat", chatMessage);

        log.debug("Mensaje de chat enviado en lobby {} por {}", code, username);
    }

    // ========== Métodos auxiliares ==========

    /**
     * Genera un código único de lobby
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (lobbyRepository.existsByCode(code));
        return code;
    }

    /**
     * Genera un código aleatorio
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * Convierte una entidad Lobby a DTO
     */
    private LobbyResponseDTO toLobbyResponseDTO(LobbyEntity lobby) {
        List<LobbyPlayerEntity> players = lobbyPlayerRepository.findByLobby(lobby);
        
        LobbyPlayerEntity host = players.stream()
                .filter(LobbyPlayerEntity::isHost)
                .findFirst()
                .orElse(null);

        return new LobbyResponseDTO(
                lobby.getId().toString(),
                lobby.getCode(),
                lobby.getMaxPlayers(),
                lobby.isPrivate(),
                lobby.getStatus().toString(),
                host != null ? toPlayerDTO(host) : null,
                players.stream().map(this::toPlayerDTO).collect(Collectors.toList())
        );
    }

    /**
     * Convierte un LobbyPlayerEntity a PlayerDTO
     */
    private PlayerDTO toPlayerDTO(LobbyPlayerEntity player) {
        return new PlayerDTO(
                player.getUser().getId().toString(),
                player.getUser().getUsername(),
                player.isReady(),
                player.isHost(),
                player.getUser().getLevel(),
                player.getUser().getScore()
        );
    }
}
