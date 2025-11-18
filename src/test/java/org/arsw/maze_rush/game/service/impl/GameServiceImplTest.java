package org.arsw.maze_rush.game.service.impl;

import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.repository.GameRepository;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.repository.LobbyRepository;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.service.MazeService;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceImplTest {

    private GameRepository gameRepository;
    private LobbyRepository lobbyRepository;
    private MazeService mazeService;

    private GameServiceImpl service;

    @BeforeEach
    void setup() {
        gameRepository = mock(GameRepository.class);
        lobbyRepository = mock(LobbyRepository.class);
        mazeService = mock(MazeService.class);

        service = new GameServiceImpl(gameRepository, lobbyRepository, mazeService);
    }


    // startGame
    
    @Test
    void testStartGame_OK() {

        // Lobby con jugadores suficientes
        LobbyEntity lobby = new LobbyEntity();

        UserEntity u1 = new UserEntity();
        u1.setUsername("user1");

        UserEntity u2 = new UserEntity();
        u2.setUsername("user2");

        lobby.setPlayers(new HashSet<>(List.of(u1, u2)));

        lobby.setMazeSize("MEDIUM");

        when(lobbyRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(lobby));

        when(gameRepository.findByLobby_Code("ABC123"))
                .thenReturn(Optional.empty());

        when(gameRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameEntity game = service.startGame("ABC123");

        assertNotNull(game);
        assertEquals("EN_CURSO", game.getStatus());
        assertTrue(game.getPlayers().contains(u1));
        assertTrue(game.getPlayers().contains(u2));
    }

    @Test
    void testStartGame_LobbyNotFound() {
        when(lobbyRepository.findByCode("X")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.startGame("X"));
    }

    @Test
    void testStartGame_NotEnoughPlayers() {
        LobbyEntity lobby = new LobbyEntity();
        UserEntity u1 = new UserEntity();
        u1.setUsername("user1");
        lobby.setPlayers(Set.of(u1));

        when(lobbyRepository.findByCode("ABC")).thenReturn(Optional.of(lobby));
        assertThrows(IllegalStateException.class, () -> service.startGame("ABC"));
    }

    @Test
    void testStartGame_ExistingActiveGame() {
        LobbyEntity lobby = new LobbyEntity();
        UserEntity u1 = new UserEntity();
        u1.setUsername("user1");
        UserEntity u2 = new UserEntity();
        u2.setUsername("user2");
        lobby.setPlayers(Set.of(u1,u2));

        GameEntity existing = GameEntity.builder().status("EN_CURSO").build();

        when(lobbyRepository.findByCode("ABC")).thenReturn(Optional.of(lobby));
        when(gameRepository.findByLobby_Code("ABC")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> service.startGame("ABC"));
    }

    @Test
    void testStartGame_GeneratesMazeIfNull() {
        LobbyEntity lobby = new LobbyEntity();
        lobby.setMazeSize("SMALL");
        UserEntity u1 = new UserEntity();
        u1.setUsername("user1");

        UserEntity u2 = new UserEntity();
        u2.setUsername("user2");

        lobby.setPlayers(Set.of(u1, u2));
        lobby.setMaze(null);

        MazeEntity generatedMaze = new MazeEntity();

        when(lobbyRepository.findByCode("ABC")).thenReturn(Optional.of(lobby));
        when(gameRepository.findByLobby_Code("ABC")).thenReturn(Optional.empty());
        when(mazeService.generateMaze("SMALL")).thenReturn(generatedMaze);
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GameEntity game = service.startGame("ABC");

        assertNotNull(game.getMaze());
        assertEquals(generatedMaze, game.getMaze());
        verify(mazeService, times(1)).generateMaze("SMALL");
    }

    // getGameById

    @Test
    void testGetGameById_OK() {
        UUID id = UUID.randomUUID();
        GameEntity game = new GameEntity();
        game.setId(id);

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        GameEntity result = service.getGameById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void testGetGameById_NotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getGameById(id));
    }

    // finishGame

    @Test
    void testFinishGame_OK() {
        UUID id = UUID.randomUUID();

        LobbyEntity lobby = new LobbyEntity();
        lobby.setStatus("EN_CURSO");

        GameEntity game = GameEntity.builder()
                .id(id)
                .status("EN_CURSO")
                .lobby(lobby)
                .build();

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(lobbyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GameEntity result = service.finishGame(id);

        assertEquals("FINALIZADO", result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertEquals("FINALIZADO", lobby.getStatus());
    }

    @Test
    void testFinishGame_AlreadyFinished() {
        UUID id = UUID.randomUUID();
        GameEntity game = GameEntity.builder().status("FINALIZADO").build();

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> service.finishGame(id));
    }

    @Test
    void testFinishGame_NotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.finishGame(id));
    }

    @Test
    void testFinishGame_LobbyNull() {
        UUID id = UUID.randomUUID();

        GameEntity game = GameEntity.builder()
                .id(id)
                .status("EN_CURSO")
                .lobby(null)   // lobby es null
                .build();

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GameEntity result = service.finishGame(id);

        assertEquals("FINALIZADO", result.getStatus());
        verify(lobbyRepository, never()).save(any()); // no debe llamarse
    }
}
