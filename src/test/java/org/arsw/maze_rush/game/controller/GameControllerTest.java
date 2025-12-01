package org.arsw.maze_rush.game.controller;

import org.arsw.maze_rush.game.dto.GameResponseDTO;
import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.game.service.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private final String lobbyCode = "XYZ789";
    private final UUID gameID = UUID.fromString("b6f1f1c2-4b32-4a4e-b3c3-1c53e5b8477d");

    @Mock
    private GameEntity mockGameEntity;
    @Mock
    private GameResponseDTO mockGameResponseDTO;



    @Test
    void startGame_Successful() {

        when(gameService.startGame(lobbyCode)).thenReturn(mockGameEntity);

        try (MockedStatic<GameResponseDTO> mockedDto = Mockito.mockStatic(GameResponseDTO.class)) {
            mockedDto.when(() -> GameResponseDTO.fromEntity(mockGameEntity))
                    .thenReturn(mockGameResponseDTO);

            ResponseEntity<GameResponseDTO> response = gameController.startGame(lobbyCode);

            assertEquals(HttpStatus.OK, response.getStatusCode());

            assertNotNull(response.getBody());
            assertEquals(mockGameResponseDTO, response.getBody());

            verify(gameService).startGame(lobbyCode);
        }
    }

    @Test
    void getGameById_Successful() {

        when(gameService.getGameById(gameID)).thenReturn(mockGameEntity);

        try (MockedStatic<GameResponseDTO> mockedDto = Mockito.mockStatic(GameResponseDTO.class)) {
            mockedDto.when(() -> GameResponseDTO.fromEntity(mockGameEntity))
                    .thenReturn(mockGameResponseDTO);

            ResponseEntity<GameResponseDTO> response = gameController.getGameById(gameID);

            assertEquals(HttpStatus.OK, response.getStatusCode());

            assertEquals(mockGameResponseDTO, response.getBody());

            verify(gameService).getGameById(gameID);
        }
    }

    @Test
    void finishGame_Successful() {

        when(gameService.finishGame(gameID)).thenReturn(mockGameEntity);

        try (MockedStatic<GameResponseDTO> mockedDto = Mockito.mockStatic(GameResponseDTO.class)) {
            mockedDto.when(() -> GameResponseDTO.fromEntity(mockGameEntity))
                    .thenReturn(mockGameResponseDTO);

            ResponseEntity<GameResponseDTO> response = gameController.finishGame(gameID);

            assertEquals(HttpStatus.OK, response.getStatusCode());

            assertEquals(mockGameResponseDTO, response.getBody());

            verify(gameService).finishGame(gameID);
        }
    }
}