package org.arsw.maze_rush.game.logic.controller;

import org.arsw.maze_rush.game.logic.dto.PlayerMoveRequestDTO;
import org.arsw.maze_rush.game.logic.entities.GameState;
import org.arsw.maze_rush.game.logic.service.GameLogicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLogicControllerTest {

    @Mock
    private GameLogicService gameLogicService;

    @InjectMocks
    private GameLogicController gameLogicController;

    private final UUID gameID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
 
    @Mock
    private GameState mockGameState;
    @Mock
    private PlayerMoveRequestDTO mockMoveRequest;


    @Test
    void initializeGame_Successful() {
        when(gameLogicService.initializeGame(gameID)).thenReturn(mockGameState);

        ResponseEntity<GameState> response = gameLogicController.initializeGame(gameID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGameState, response.getBody());

        verify(gameLogicService).initializeGame(gameID);
    }

    @Test
    void movePlayer_Successful() {
        when(gameLogicService.movePlayer(gameID,mockMoveRequest)).thenReturn(mockGameState);

        ResponseEntity<GameState> response = gameLogicController.movePlayer(gameID, mockMoveRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGameState, response.getBody());

        verify(gameLogicService).movePlayer(gameID,mockMoveRequest);
    }

    @Test
    void getCurrentGameState_Successful() {
        when(gameLogicService.getCurrentState(gameID)).thenReturn(mockGameState);

        ResponseEntity<GameState> response = gameLogicController.getCurrentGameState(gameID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGameState, response.getBody());

        verify(gameLogicService).getCurrentState(gameID);
    }
}