package org.arsw.maze_rush.lobby.entities;

import org.arsw.maze_rush.users.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LobbyPlayerEntityTest {

    @Mock
    private LobbyEntity lobbyMock;

    @Mock
    private UserEntity userMock;


    @Test
    void testGettersAndSetters() {
        LobbyPlayerEntity player = new LobbyPlayerEntity();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        player.setId(id);
        player.setLobby(lobbyMock);
        player.setUser(userMock);
        player.setReady(true);
        player.setHost(true);
        player.setJoinedAt(now);

        assertEquals(id, player.getId());
        assertEquals(lobbyMock, player.getLobby());
        assertEquals(userMock, player.getUser());
        assertTrue(player.isReady());
        assertTrue(player.isHost());
        assertEquals(now, player.getJoinedAt());
        
        player.setReady(false);
        player.setHost(false);
        assertFalse(player.isReady());
        assertFalse(player.isHost());
    }

    /**
     * Test para el método @PrePersist (onCreate)
     * CASO : El ID es nulo.
     */
    @Test
    void testOnCreate_WhenIdIsNull_ShouldGenerateId() {
        LobbyPlayerEntity player = new LobbyPlayerEntity();
        assertNull(player.getId()); 
        assertNull(player.getJoinedAt());

        player.onCreate(); 

        assertNotNull(player.getId(), "El ID debería haberse generado automáticamente");
        assertNotNull(player.getJoinedAt(), "La fecha de unión debería haberse establecido");
    }

    /**
     * Test para el método @PrePersist (onCreate)
     * CASO : El ID ya existe.
     */
    @Test
    void testOnCreate_WhenIdExists_ShouldKeepId() {
        LobbyPlayerEntity player = new LobbyPlayerEntity();
        UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        player.setId(existingId);

        player.onCreate();

        assertEquals(existingId, player.getId(), "El ID existente no debería cambiar");
        assertNotNull(player.getJoinedAt(), "La fecha de unión debería haberse establecido");
    }
}