package org.arsw.maze_rush.game.logic.service.impl;

import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.entities.PlayerPosition;
import org.arsw.maze_rush.game.logic.service.GameLogicService;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.powerups.service.PowerUpService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameLogicServiceImpl implements GameLogicService {
    private final PowerUpService powerUpService;
    private final GameRepository gameRepository;
    private final Map<UUID, GameState> activeGames = new ConcurrentHashMap<>();

    public GameLogicServiceImpl(GameRepository gameRepository, PowerUpService powerUpService) {
        this.gameRepository = gameRepository;
        this.powerUpService = powerUpService;
    }

    @Override
    public GameState initializeGame(UUID gameId) {
        
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

        MazeEntity maze = game.getMaze();
        if (maze == null){
            throw new IllegalStateException("El lobby no tiene laberinto asociado");
        }
        if (game.getPlayers() == null || game.getPlayers().isEmpty()) {
            throw new IllegalStateException("El juego no tiene jugadores asignados");
        }
        List<PlayerPosition> positions = new ArrayList<>();
        int startX = maze.getStartX();
        int startY = maze.getStartY();
        for (var player : game.getPlayers()) {
            positions.add(new PlayerPosition(player, startX, startY, 0));
        }
        var powerUps = powerUpService.generatePowerUps(maze, positions);

        GameState state = new GameState();
        state.setGameId(gameId);
        state.setStatus("EN_CURSO");
        state.setPlayerPositions(positions);
        state.setPowerUps(powerUps); 

        activeGames.put(gameId, state);
        
        return state;
    }

    @Override
    public GameState movePlayer(UUID gameId, PlayerMoveRequestDTO moveRequest) {
        GameState state = activeGames.get(gameId);
        if (state == null) throw new IllegalStateException("El juego no está activo");

        PlayerPosition player = state.getPlayerPositions().stream()
                .filter(p -> p.getPlayer().getUsername().equalsIgnoreCase(moveRequest.getUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));

        // Obtener el laberinto actual del juego
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        MazeEntity maze = game.getLobby().getMaze();

        // Convertir layout a matriz
        String[] rows = maze.getLayout().split("\n");
        int height = rows.length;
        int width = rows[0].length();

        int newX = player.getX();
        int newY = player.getY();

        switch (moveRequest.getDirection().toUpperCase()) {
            case "UP" -> newY--;
            case "DOWN" -> newY++;
            case "LEFT" -> newX--;
            case "RIGHT" -> newX++;
            default -> throw new IllegalArgumentException("Dirección no válida");
        }

        // Validar límites del mapa
        if (newY < 0 || newY >= height || newX < 0 || newX >= width) {
            throw new IllegalStateException("Movimiento fuera de los límites del mapa");
        }

        // Validar colisión con pared
        char destination = rows[newY].charAt(newX);
        if (destination == '1') {
            throw new IllegalStateException("Movimiento bloqueado por una pared");
        }

        // Movimiento válido → actualizar posición
        player.setX(newX);
        player.setY(newY);

        return state;
    }

    @Override
    public GameState getCurrentState(UUID gameId) {
        return activeGames.get(gameId);
    }
}
