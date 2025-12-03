package org.arsw.maze_rush.lobby.controller;

import org.arsw.maze_rush.lobby.dto.*;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.lobby.service.LobbyService;
import org.arsw.maze_rush.users.entities.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LobbyControllerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private LobbyController controller;

    private LobbyEntity sampleLobby;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testUser", null)
        );

        sampleLobby = new LobbyEntity();
        sampleLobby.setId(UUID.randomUUID());
        sampleLobby.setCode("ABC123");
        sampleLobby.setMazeSize("Mediano");
        sampleLobby.setMaxPlayers(4);
        sampleLobby.setPublic(true);
        sampleLobby.setStatus("EN_ESPERA");
        sampleLobby.setCreatorUsername("testUser");
        sampleLobby.setCreatedAt(LocalDateTime.now());

        UserEntity player = new UserEntity();
        player.setUsername("player1");

        sampleLobby.addPlayer(player);
    }

    // TEST: createLobby
    @Test
    void testCreateLobby_ok() {

        LobbyRequestDTO request = new LobbyRequestDTO();
        request.setMazeSize("Mediano");
        request.setMaxPlayers(4);
        request.setPublic(true);
        request.setStatus("EN_ESPERA");

        when(lobbyService.createLobby(any(), anyInt(), anyBoolean(), any(), eq("testUser")))
                .thenReturn(sampleLobby);

        ResponseEntity<LobbyWithPlayersResponseDTO> response =
                controller.createLobby(request);

        assertEquals("ABC123", response.getBody().getCode());
        assertEquals(1, response.getBody().getPlayers().size());
        verify(lobbyService).createLobby("Mediano", 4, true, "EN_ESPERA", "testUser");
    }

    //  TEST: getAllLobbies
    @Test
    void testGetAllLobbies_ok() {

        when(lobbyService.getAllLobbies()).thenReturn(List.of(sampleLobby));

        ResponseEntity<List<LobbyResponseDTO>> response =
                controller.getAllLobbies();

        assertEquals(1, response.getBody().size());
        assertEquals("ABC123", response.getBody().get(0).getCode());
        assertEquals(1, response.getBody().get(0).getCurrentPlayers());
    }

    // TEST: getLobbyByCode
    @Test
    void testGetLobbyByCode_ok() {

        when(lobbyService.getLobbyByCode("ABC123")).thenReturn(sampleLobby);

        ResponseEntity<LobbyWithPlayersResponseDTO> response =
                controller.getLobbyByCode("ABC123");

        assertEquals("Mediano", response.getBody().getMazeSize());
        assertEquals("player1", response.getBody().getPlayers().get(0));
    }

    //  TEST: joinLobby
    @Test
    void testJoinLobby_ok() {

        when(lobbyService.joinLobbyByCode("ABC123", "testUser"))
                .thenReturn(sampleLobby);

        ResponseEntity<LobbyWithPlayersResponseDTO> response =
                controller.joinLobby("ABC123");

        assertEquals("ABC123", response.getBody().getCode());
        assertEquals(1, response.getBody().getPlayers().size());
    }

    // TEST: leaveLobby
    @Test
    void testLeaveLobby_ok() {

        ResponseEntity<String> response = controller.leaveLobby("ABC123");

        assertEquals("El jugador testUser ha salido del lobby ABC123",
                response.getBody());
        verify(lobbyService).leaveLobby("ABC123", "testUser");
    }

    // TEST: removePlayerFromLobby
    @Test
    void testRemovePlayerFromLobby_ok() {

        UUID lobbyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseEntity<String> response =
                controller.removePlayerFromLobby(lobbyId, userId);

        assertEquals("Jugador removido correctamente del lobby", response.getBody());
        verify(lobbyService).removePlayerFromLobby(lobbyId, userId);
    }
}
