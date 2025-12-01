package org.arsw.maze_rush.game.dto;

import org.arsw.maze_rush.game.entities.GameEntity;
import org.arsw.maze_rush.lobby.entities.LobbyEntity;
import org.arsw.maze_rush.maze.dto.MazeResponseDTO;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameResponseDTOTest {

    private GameEntity mockGame;
    private LobbyEntity mockLobby;
    private UserEntity mockUser1;
    private UserEntity mockUser2;
    private MazeEntity mockMaze;

    private final UUID gameID = UUID.randomUUID();
    private final String lobbyCode = "XYZ789";
    private final String status = "EN_CURSO";
    private final LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
    private final LocalDateTime finishedAt = null;
    private final LocalDateTime completedAt = LocalDateTime.now();
    private final String username1 = "playerA";
    private final String username2 = "playerB";
    private final MazeResponseDTO mockMazeResponse = new MazeResponseDTO();
    
    @BeforeEach
    void setUp() {
        mockGame = mock(GameEntity.class);
        mockLobby = mock(LobbyEntity.class);
        mockUser1 = mock(UserEntity.class);
        mockUser2 = mock(UserEntity.class);
        mockMaze = mock(MazeEntity.class);

        when(mockGame.getId()).thenReturn(gameID);
        when(mockGame.getStatus()).thenReturn(status);
        when(mockGame.getStartedAt()).thenReturn(startedAt);
        when(mockGame.getFinishedAt()).thenReturn(finishedAt);
        when(mockGame.getLobby()).thenReturn(mockLobby);
        when(mockGame.getMaze()).thenReturn(mockMaze);
        
        when(mockLobby.getCode()).thenReturn(lobbyCode);

        when(mockUser1.getUsername()).thenReturn(username1);
        when(mockUser2.getUsername()).thenReturn(username2);
        
        Set<UserEntity> playersSet = new HashSet<>(Arrays.asList(mockUser1, mockUser2));
        when(mockGame.getPlayers()).thenReturn(playersSet); 
    }

    //  Test de Mapeo fromEntity (Lógica de Negocio)
    @Test
    void testFromEntity_FullMapping() {
        try (MockedStatic<MazeResponseDTO> mockedMazeDto = mockStatic(MazeResponseDTO.class)) {
            mockedMazeDto.when(() -> MazeResponseDTO.fromEntity(mockMaze)).thenReturn(mockMazeResponse);

            GameResponseDTO dto = GameResponseDTO.fromEntity(mockGame);

            assertNotNull(dto);
            assertEquals(gameID, dto.getId());
            assertEquals(status, dto.getStatus());
            assertEquals(startedAt, dto.getStartedAt());
            assertEquals(finishedAt, dto.getFinishedAt());
            assertEquals(lobbyCode, dto.getLobbyCode());
            assertEquals(mockMazeResponse, dto.getMaze());

            List<String> expectedPlayers = Arrays.asList(username1, username2);
            List<String> actualPlayers = dto.getPlayers();

            assertEquals(expectedPlayers.size(), actualPlayers.size(), "El número de jugadores debe coincidir.");
            assertTrue(actualPlayers.containsAll(expectedPlayers), "La lista de jugadores debe contener los usernames esperados.");
            
            verify(mockGame).getLobby();
            verify(mockGame).getPlayers();
        }
    }

    @Test
    void testFromEntity_HandlesNullLobbyAndEmptyPlayers() {
        when(mockGame.getLobby()).thenReturn(null);
        
        when(mockGame.getPlayers()).thenReturn(Collections.emptySet());

        try (MockedStatic<MazeResponseDTO> mockedMazeDto = mockStatic(MazeResponseDTO.class)) {
            mockedMazeDto.when(() -> MazeResponseDTO.fromEntity(mockMaze)).thenReturn(mockMazeResponse);

            GameResponseDTO dto = GameResponseDTO.fromEntity(mockGame);

            assertNull(dto.getLobbyCode(), "El lobbyCode debe ser null si LobbyEntity es null.");
            assertTrue(dto.getPlayers().isEmpty(), "La lista de jugadores debe estar vacía si la entidad es vacía.");
        }
    }
    
    @Test
    void testFromEntity_HandlesFinishedGame() {
        when(mockGame.getFinishedAt()).thenReturn(completedAt);
        
        try (MockedStatic<MazeResponseDTO> mockedMazeDto = mockStatic(MazeResponseDTO.class)) {
            mockedMazeDto.when(() -> MazeResponseDTO.fromEntity(mockMaze)).thenReturn(mockMazeResponse);

            GameResponseDTO dto = GameResponseDTO.fromEntity(mockGame);

            assertNotNull(dto);
            assertEquals(completedAt, dto.getFinishedAt(), "Debe mapear el tiempo de finalización correctamente.");
        }
    }
    
    //  Tests de Lombok (@Data: Getters, Setters, Equals, HashCode, ToString)
    @Test
    void testGettersAndSetters() {
        GameResponseDTO dto = new GameResponseDTO();
        
        dto.setId(gameID);
        dto.setLobbyCode(lobbyCode);
        dto.setStatus(status);
        dto.setStartedAt(startedAt);
        dto.setFinishedAt(completedAt);
        dto.setPlayers(Arrays.asList(username1));
        dto.setMaze(mockMazeResponse);

        assertEquals(gameID, dto.getId());
        assertEquals(lobbyCode, dto.getLobbyCode());
        assertEquals(status, dto.getStatus());
        assertEquals(startedAt, dto.getStartedAt());
        assertEquals(completedAt, dto.getFinishedAt());
        assertEquals(Arrays.asList(username1), dto.getPlayers());
        assertEquals(mockMazeResponse, dto.getMaze());
    }

    @Test
    void testEqualsAndHashCode() {
        GameResponseDTO dto1 = new GameResponseDTO();
        dto1.setId(gameID);
        dto1.setLobbyCode(lobbyCode);
        dto1.setStatus(status);
        
        GameResponseDTO dto2 = new GameResponseDTO();
        dto2.setId(gameID);
        dto2.setLobbyCode(lobbyCode);
        dto2.setStatus(status);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        dto2.setLobbyCode("DIFF");
        assertNotEquals(dto1, dto2);
        
        assertEquals(dto1, dto1);
        assertNotEquals(null, dto1);
        assertNotEquals(dto1, new Object());
    }

    @Test
    void testToStringIsCorrect() {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setId(gameID);
        dto.setLobbyCode(lobbyCode);
        dto.setStatus(status);

        String result = dto.toString();
        
        assertNotNull(result);
        assertTrue(result.contains(gameID.toString()));
        assertTrue(result.contains(lobbyCode));
        assertTrue(result.contains(status));
    }
}