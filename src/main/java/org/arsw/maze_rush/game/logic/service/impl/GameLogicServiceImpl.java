package org.arsw.maze_rush.game.logic.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.arsw.maze_rush.game.dto.GameNotificationDTO;
import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO;
import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.service.GameLogicService;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.game.service.GameSessionManager;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.entities.PowerUp;
import org.arsw.maze_rush.powerups.entities.PowerUpType;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GameLogicServiceImpl implements GameLogicService {
    private static final String UP = "UP";
    private static final String DOWN = "DOWN";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";

    private final GameRepository gameRepository;
    private final PowerUpService powerUpService;
    private final GameSessionManager gameSessionManager;
    private final SimpMessagingTemplate messagingTemplate; 

    public GameLogicServiceImpl(
            GameRepository gameRepository, 
            PowerUpService powerUpService,
            GameSessionManager gameSessionManager,
            SimpMessagingTemplate messagingTemplate) { 
        this.gameRepository = gameRepository;
        this.powerUpService = powerUpService;
        this.gameSessionManager = gameSessionManager;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public GameState initializeGame(UUID gameId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

        MazeEntity maze = game.getMaze();
        if (maze == null) throw new IllegalStateException("El lobby no tiene laberinto asociado");
        
        String lobbyCode = game.getLobby().getCode();
        
        // Generar PowerUps
        List<PowerUp> powerUps = powerUpService.generatePowerUps(maze); 

        // Guardar PowerUps en SessionManager
        gameSessionManager.setPowerUps(lobbyCode, powerUps);

        // Plasmar PowerUps en el layout
        char[][] matrix = convertJSONLayoutToMatrix(maze.getLayout(), maze.getWidth(), maze.getHeight());
        applyPowerUpsToLayout(matrix, powerUps);
        maze.setLayout(convertMatrixToJSON(matrix));
        gameRepository.save(game);

        return buildGameStateFromManager(gameId, game);
    }

    @Override
    public GameState movePlayer(UUID gameId, PlayerMoveRequestDTO moveRequest) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        String lobbyCode = game.getLobby().getCode();
        String username = moveRequest.getUsername();

        PlayerGameStateDTO playerState = gameSessionManager.getPlayer(lobbyCode, username);
        if (playerState == null) throw new IllegalStateException("Jugador no encontrado en la sesión");

        // Antes de calcular movimiento, limpiamos efectos y verificamos freeze
        gameSessionManager.cleanExpiredEffects(lobbyCode, username);
        
        if (playerState.getActiveEffects().containsKey(PowerUpType.FREEZE)) {
            log.info("Jugador {} está congelado. Turno perdido.", username);
            return buildGameStateFromManager(gameId, game);
        }

        // Validar CONFUSIÓN (Invertir controles)
        String direction = moveRequest.getDirection().toUpperCase();
        if (playerState.getActiveEffects().containsKey(PowerUpType.CONFUSION)) {
            log.info("Jugador {} está confuso. Controles invertidos.", username);
            direction = invertDirection(direction);
        }

        // Calcular nueva posición usando la dirección (posiblemente invertida)
        int newX = playerState.getPosition().getX();
        int newY = playerState.getPosition().getY();

        switch (direction) {
            case UP -> newY--;
            case DOWN -> newY++;
            case LEFT -> newX--;
            case RIGHT -> newX++;
            default -> throw new IllegalArgumentException("Dirección no válida");
        }

        // Validar Muros
        MazeEntity maze = game.getLobby().getMaze();
        validateMove(maze, newX, newY);

        //  Recolección y APLICACIÓN de efectos
        PowerUp collected = gameSessionManager.checkAndCollectPowerUp(lobbyCode, newX, newY);
        
        if (collected != null) {
            log.info("Jugador {} recogió {}", username, collected.getType());
            
            // APLICAR EL EFECTO 
            switch (collected.getType()) {
                case CLEAR_FOG -> gameSessionManager.applyEffect(
                        lobbyCode, username, PowerUpType.CLEAR_FOG, collected.getDuration());
                
                case FREEZE -> gameSessionManager.applyEffectToOpponents(
                        lobbyCode, username, PowerUpType.FREEZE, collected.getDuration());
                
                case CONFUSION -> gameSessionManager.applyEffectToOpponents(
                        lobbyCode, username, PowerUpType.CONFUSION, collected.getDuration());
            }

            sendPowerUpNotification(lobbyCode, username, collected);

            // Actualizar visualmente el mapa
            updateMazeLayoutVisuals(maze, newX, newY);
            gameRepository.save(game);
        }

        // Actualizar posición en Manager
        gameSessionManager.updatePlayerPosition(lobbyCode, username, new PositionDTO(newX, newY));

        return buildGameStateFromManager(gameId, game);
    }

    @Override
    public GameState getCurrentState(UUID gameId) {
        GameEntity game = gameRepository.findById(gameId).orElseThrow();
        return buildGameStateFromManager(gameId, game);
    }

    // --- Métodos Auxiliares ---

    private String invertDirection(String dir) {
        return switch (dir) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            default -> dir;
        };
    }

    private void validateMove(MazeEntity maze, int x, int y) {
        String[] rows = maze.getLayout().split("\n");
        if (y < 0 || y >= rows.length || x < 0 || x >= rows[0].length()) {
            throw new IllegalStateException("Movimiento fuera de límites");
        }
        if (rows[y].charAt(x) == '1') {
            throw new IllegalStateException("Movimiento bloqueado por pared");
        }
    }

    private void updateMazeLayoutVisuals(MazeEntity maze, int x, int y) {
        char[][] matrix = convertJSONLayoutToMatrix(maze.getLayout(), maze.getWidth(), maze.getHeight());
        matrix[y][x] = '0'; 
        maze.setLayout(convertMatrixToJSON(matrix));
    }

    private GameState buildGameStateFromManager(UUID gameId, GameEntity game) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setStatus("EN_CURSO");
        String lobbyCode = game.getLobby().getCode();
        if (game.getLobby().getMaze() != null) {
            state.setCurrentLayout(game.getLobby().getMaze().getLayout());
        }
        List<PlayerGameStateDTO> livePlayers = gameSessionManager.getPlayers(lobbyCode);
        state.setPlayers(livePlayers);
        
        return state;
    }

    private char[][] convertJSONLayoutToMatrix(String layout, int width, int height) {
        String[] rows = layout.split("\n");
        char[][] matrix = new char[height][width];
        for (int y = 0; y < height; y++) {
            matrix[y] = rows[y].toCharArray(); 
        }
        return matrix;
    }

    private void applyPowerUpsToLayout(char[][] matrix, List<PowerUp> powerUps) {
        for (PowerUp pu : powerUps) {
            matrix[pu.getY()][pu.getX()] = 'P';
        }
    }

    private String convertMatrixToJSON(char[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : matrix) {
            sb.append(new String(row)).append("\n");
        }
        return sb.toString().trim();
    }

    private void sendPowerUpNotification(String lobbyCode, String username, PowerUp powerUp) {
        String message = "";
        switch (powerUp.getType()) {
            case CLEAR_FOG -> message = username + " encontró una linterna y despejó la niebla.";
            case FREEZE -> message = "¡" + username + " ha CONGELADO a todos los rivales!";
            case CONFUSION -> message = "¡Cuidado! " + username + " lanzó un hechizo de CONFUSIÓN.";
        }

        GameNotificationDTO notification = GameNotificationDTO.builder()
                .type("POWER_UP")
                .message(message)
                .sourceUser(username)
                .powerUpType(powerUp.getType())
                .build();
        messagingTemplate.convertAndSend("/topic/game/" + lobbyCode + "/notifications", notification);
    }


}