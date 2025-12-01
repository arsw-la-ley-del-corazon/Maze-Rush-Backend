package org.arsw.maze_rush.maze.dto;

import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MazeResponseDTOTest {

    @Mock
    private MazeEntity mazeEntityMock;

    @Test
    void testLombokGeneratedMethods() {
        UUID id = UUID.randomUUID();
        
        //  Instanciación y Setters
        MazeResponseDTO dto1 = new MazeResponseDTO();
        dto1.setId(id);
        dto1.setSize("MEDIUM");
        dto1.setWidth(20);
        dto1.setHeight(20);
        dto1.setLayout("{\"walls\": []}");

        MazeResponseDTO dto2 = new MazeResponseDTO();
        dto2.setId(id);
        dto2.setSize("MEDIUM");
        dto2.setWidth(20);
        dto2.setHeight(20);
        dto2.setLayout("{\"walls\": []}");

        MazeResponseDTO dto3 = new MazeResponseDTO();
        dto3.setId(UUID.randomUUID()); 

        //  Getters
        assertEquals(id, dto1.getId());
        assertEquals("MEDIUM", dto1.getSize());
        assertEquals(20, dto1.getWidth());
        assertEquals(20, dto1.getHeight());
        assertEquals("{\"walls\": []}", dto1.getLayout());

        //  Equals y HashCode
        assertEquals(dto1, dto2); 
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        
        boolean isReflexive = dto1.equals(dto1);
        assertTrue(isReflexive); 
        assertNotEquals(dto1, dto3); 
        assertNotEquals(null, dto1); 
        assertNotEquals(dto1, new Object()); 

        //  ToString
        String stringResult = dto1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("MEDIUM"));
        assertTrue(stringResult.contains("MazeResponseDTO"));
    }


    @Test
    void testFromEntity_NullInput() {
        MazeResponseDTO result = MazeResponseDTO.fromEntity(null);
        assertNull(result, "Si la entidad es null, el DTO debe ser null");
    }

    @Test
    void testFromEntity_ValidInput() {
        UUID id = UUID.randomUUID();
        String size = "LARGE";
        int width = 50;
        int height = 50;
        String layout = "sample-layout-json";

        when(mazeEntityMock.getId()).thenReturn(id);
        when(mazeEntityMock.getSize()).thenReturn(size);
        when(mazeEntityMock.getWidth()).thenReturn(width);
        when(mazeEntityMock.getHeight()).thenReturn(height);
        when(mazeEntityMock.getLayout()).thenReturn(layout);

        MazeResponseDTO result = MazeResponseDTO.fromEntity(mazeEntityMock);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(size, result.getSize());
        assertEquals(width, result.getWidth());
        assertEquals(height, result.getHeight());
        assertEquals(layout, result.getLayout());
    }
}