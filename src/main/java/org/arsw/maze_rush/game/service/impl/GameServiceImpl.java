package org.arsw.maze_rush.game.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.game.service.GameService;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.service.MazeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final MazeService mazeService;

    public GameServiceImpl(GameRepository gameRepository,
                           LobbyRepository lobbyRepository,
                           MazeService mazeService) {
        this.gameRepository = gameRepository;
        this.lobbyRepository = lobbyRepository;
        this.mazeService = mazeService;

    }

    @Override
    public GameEntity startGame(String lobbyCode) {
        LobbyEntity lobby = lobbyRepository.findByCode(lobbyCode)
                .orElseThrow(() -> new NotFoundException("No se encontró el lobby con código: " + lobbyCode));

        if (lobby.getPlayers().size() < 2) {
            throw new IllegalStateException("Se necesitan al menos 2 jugadores para iniciar el juego");
        }

        gameRepository.findByLobby_Code(lobbyCode).ifPresent(existingGame -> {
            if (!"FINALIZADO".equalsIgnoreCase(existingGame.getStatus())) {
                throw new IllegalStateException("Ya existe un juego activo para este lobby");
            }
        });

        MazeEntity maze = lobby.getMaze();
        
        if (maze == null) {
        maze = mazeService.generateMaze(lobby.getMazeSize());
        lobby.setMaze(maze);
    }

        lobby.setStatus("EN_JUEGO");
        lobbyRepository.save(lobby);

        GameEntity game = GameEntity.builder()
                .lobby(lobby)
                .maze(maze)   
                .players(new HashSet<>(lobby.getPlayers()))
                .status("EN_CURSO")
                .startedAt(LocalDateTime.now())
                .build();

        return gameRepository.save(game);
    }

    @Override
    @Transactional(readOnly = true)
    public GameEntity getGameById(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Juego no encontrado con ID: " + gameId));
    }


     @Override
        public GameEntity finishGame(UUID id) {
            GameEntity game = gameRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Juego no encontrado con ID: " + id));

            if ("FINALIZADO".equalsIgnoreCase(game.getStatus())) {
                throw new IllegalStateException("El juego ya está finalizado");
            }

            game.setStatus("FINALIZADO");
            game.setFinishedAt(LocalDateTime.now());

    
            LobbyEntity lobby = game.getLobby();
            if (lobby != null) {
                lobby.setStatus("FINALIZADO");
                lobbyRepository.save(lobby);
            }

            return gameRepository.save(game);
        }

}
