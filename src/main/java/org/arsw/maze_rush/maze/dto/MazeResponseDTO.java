package org.arsw.maze_rush.maze.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.arsw.maze_rush.maze.entities.MazeEntity;

import java.util.UUID;

@Data
@Schema(name = "MazeResponse", description = "Información del laberinto generado para un juego.")
public class MazeResponseDTO {

    @Schema(description = "ID único del laberinto.", example = "5a8e7e0b-cc56-44de-9f0a-4a7d9b8e9e41")
    private UUID id;

    @Schema(description = "Tamaño del laberinto (SMALL, MEDIUM, LARGE).", example = "MEDIUM")
    private String size;
 
    @Schema(description = "Ancho del laberinto (número de celdas).", example = "20")
    private int width;

    @Schema(description = "Altura del laberinto (número de celdas).", example = "20")
    private int height;

    @Schema(description = "Estructura del laberinto en formato JSON (paredes, caminos, etc.).")
    private String layout;

    public static MazeResponseDTO fromEntity(MazeEntity maze) {
        if (maze == null) return null;

        MazeResponseDTO dto = new MazeResponseDTO();
        dto.setId(maze.getId());
        dto.setSize(maze.getSize());
        dto.setWidth(maze.getWidth());
        dto.setHeight(maze.getHeight());
        dto.setLayout(maze.getLayout());
        return dto;
    }
}
