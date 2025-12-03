package org.arsw.maze_rush.game.service;

import org.arsw.maze_rush.game.dto.GameSyncDTO;
import org.arsw.maze_rush.game.dto.PlayerGameStateDTO;
import org.arsw.maze_rush.game.dto.PositionDTO; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GameSyncServiceTest {

    private static final String LOBBY_CODE_1 = "LOBBY1";
    private static final String LOBBY_CODE_2 = "LOBBY2";
    private static final String DESTINATION_1 = "/topic/game/" + LOBBY_CODE_1 + "/sync";

    @Mock
    private GameSessionManager sessionManager;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameSyncService gameSyncService;

    private PositionDTO mockPosition; 
    private List<PlayerGameStateDTO> players;

    @BeforeEach
    void setUp() {
        //  Inicializa la posición requerida
        mockPosition = new PositionDTO(10, 5); 

        //  Inicializa un estado de jugador simulado usando el constructor CORREGIDO
        PlayerGameStateDTO playerState = new PlayerGameStateDTO("testUser", mockPosition);
        
        //  Asigna la lista
        players = Collections.singletonList(playerState);
    }

    // Tests para syncGameStates 

    @Test
    void testSyncGameStates_ShouldSyncMultipleActiveSessions() {
        Set<String> activeSessions = new HashSet<>(Arrays.asList(LOBBY_CODE_1, LOBBY_CODE_2));
        
        // Simula que ambas sesiones tienen jugadores
        when(sessionManager.getActiveSessions()).thenReturn(activeSessions);
        when(sessionManager.getPlayers(LOBBY_CODE_1)).thenReturn(players);
        when(sessionManager.getPlayers(LOBBY_CODE_2)).thenReturn(players);

        gameSyncService.syncGameStates();

        // Assert: Verifica que se llamó a la sincronización de mensajes para ambas sesiones
        verify(messagingTemplate, times(1)).convertAndSend(eq(DESTINATION_1), any(GameSyncDTO.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/game/" + LOBBY_CODE_2 + "/sync"), any(GameSyncDTO.class));
    }

    @Test
    void testSyncGameStates_ShouldHandleNoActiveSessions() {
        when(sessionManager.getActiveSessions()).thenReturn(Collections.emptySet());

        gameSyncService.syncGameStates();

        // Assert: Verifica que no se intentó obtener jugadores ni enviar mensajes
        verify(sessionManager, never()).getPlayers(anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
    
    @Test
    void testSyncGameStates_ShouldSkipSessionWithNoPlayers() {
        Set<String> activeSessions = Collections.singleton(LOBBY_CODE_1);
        when(sessionManager.getActiveSessions()).thenReturn(activeSessions);
        // Simula que la sesión no tiene jugadores
        when(sessionManager.getPlayers(LOBBY_CODE_1)).thenReturn(Collections.emptyList());

        gameSyncService.syncGameStates();

        // Assert: Verifica que no se intentó enviar el mensaje si la lista de jugadores está vacía
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    //  Tests para forceSyncSession 

    @Test
    void testForceSyncSession_ShouldSendSyncMessage() {
        when(sessionManager.getPlayers(LOBBY_CODE_1)).thenReturn(players);
        ArgumentCaptor<GameSyncDTO> syncCaptor = ArgumentCaptor.forClass(GameSyncDTO.class);

        gameSyncService.forceSyncSession(LOBBY_CODE_1);

        // Assert: Verifica que el mensaje se envió a la ruta correcta
        verify(messagingTemplate, times(1)).convertAndSend(
            eq(DESTINATION_1), 
            syncCaptor.capture()
        );
        // Verifica que el DTO enviado contiene el estado de los jugadores
        assertEquals(players, syncCaptor.getValue().getPlayers(), "El DTO debe contener el estado de los jugadores");
    }

    // Tests para cleanupInactiveSessions 
    @Test
    void testCleanupInactiveSessions_ShouldClearEmptySession() {
        Set<String> activeSessions = Collections.singleton(LOBBY_CODE_1);
        when(sessionManager.getActiveSessions()).thenReturn(activeSessions);
        // Simula que la sesión LOBBY1 no tiene jugadores
        when(sessionManager.getPlayerCount(LOBBY_CODE_1)).thenReturn(0);

        gameSyncService.cleanupInactiveSessions();

        // Assert:Verifica que se llamó al método de limpieza para la sesión vacía
        verify(sessionManager, times(1)).clearSession(LOBBY_CODE_1);
    }

    @Test
    void testCleanupInactiveSessions_ShouldNotClearActiveSession() {
        Set<String> activeSessions = Collections.singleton(LOBBY_CODE_2);
        when(sessionManager.getActiveSessions()).thenReturn(activeSessions);
        // Simula que la sesión LOBBY2 tiene 1 jugador
        when(sessionManager.getPlayerCount(LOBBY_CODE_2)).thenReturn(1);

        gameSyncService.cleanupInactiveSessions();

        // Assert:  Verifica que NO se llamó al método de limpieza
        verify(sessionManager, never()).clearSession(LOBBY_CODE_2);
    }
    
    // Test de manejo de excepciones en syncGameStates
    
    @Test
    void testSyncGameStates_ShouldHandleExceptionDuringSessionSync() {
        Set<String> activeSessions = new HashSet<>(Arrays.asList(LOBBY_CODE_1, LOBBY_CODE_2));
        when(sessionManager.getActiveSessions()).thenReturn(activeSessions);
        
        when(sessionManager.getPlayers(LOBBY_CODE_1)).thenThrow(new RuntimeException("Simulated error"));
        
        when(sessionManager.getPlayers(LOBBY_CODE_2)).thenReturn(players);

        // El test debe pasar si la excepción es capturada y el loop continúa
        gameSyncService.syncGameStates();

        // Assert: Verifica que el loop continuó y la segunda sesión se sincronizó
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/game/" + LOBBY_CODE_2 + "/sync"), any(GameSyncDTO.class));
        
    }
}