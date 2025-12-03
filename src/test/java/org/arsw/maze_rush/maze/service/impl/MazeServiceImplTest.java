package org.arsw.maze_rush.maze.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.arsw.maze_rush.common.exceptions.MazeGenerationException;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.repository.MazeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MazeServiceImplTest {

    @Mock
    private MazeRepository mazeRepository;

    @InjectMocks
    private MazeServiceImpl mazeService;

    private MazeEntity savedMaze;

    @BeforeEach
    void setup() {
        savedMaze = MazeEntity.builder()
                .id(UUID.randomUUID())
                .size("MEDIUM")
                .width(25)
                .height(25)
                .layout("[[0,1],[1,0]]")
                .startX(1)
                .startY(1)
                .goalX(23)
                .goalY(23)
                .build();
    }

    // generateMaze SMALL
    @Test
    void testGenerateMaze_Small() {
        when(mazeRepository.save(any(MazeEntity.class))).thenReturn(savedMaze);

        MazeEntity maze = mazeService.generateMaze("SMALL");

        assertNotNull(maze);
        assertEquals("MEDIUM", maze.getSize()); // returned mock
        verify(mazeRepository).save(any(MazeEntity.class));
    }

    // generateMaze LARGE
    @Test
    void testGenerateMaze_Large() {
        when(mazeRepository.save(any(MazeEntity.class))).thenReturn(savedMaze);

        MazeEntity maze = mazeService.generateMaze("LARGE");

        assertNotNull(maze);
        verify(mazeRepository).save(any(MazeEntity.class));
    }

    // generateMaze DEFAULT (size no reconocido)
    @Test
    void testGenerateMaze_Default() {
        when(mazeRepository.save(any(MazeEntity.class))).thenReturn(savedMaze);

        MazeEntity maze = mazeService.generateMaze("UNKNOWN");

        assertNotNull(maze);
        verify(mazeRepository, times(1)).save(any(MazeEntity.class));
    }

    // Serialización falla → MazeGenerationException
    @Test
    void testGenerateMaze_SerializationError() throws Exception {
        MazeServiceImpl serviceSpy = spy(mazeService);

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        doThrow(JsonProcessingException.class).when(mockMapper)
                .writeValueAsString(any(int[][].class));

        var field = MazeServiceImpl.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(serviceSpy, mockMapper);

        assertThrows(MazeGenerationException.class,
                () -> serviceSpy.generateMaze("SMALL"));
    }

    // getMazeById OK
    @Test
    void testGetMazeById_OK() {
        UUID id = savedMaze.getId();
        when(mazeRepository.findById(id)).thenReturn(Optional.of(savedMaze));

        MazeEntity maze = mazeService.getMazeById(id);

        assertEquals(savedMaze.getId(), maze.getId());
        verify(mazeRepository).findById(id);
    }

    // getMazeById NOT FOUND

    @Test
    void testGetMazeById_NotFound() {
        UUID id = UUID.randomUUID();
        when(mazeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> mazeService.getMazeById(id));
    }
}
