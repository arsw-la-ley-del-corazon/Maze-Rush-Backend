package org.arsw.maze_rush.users.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private String username;
    private String email;
    private int score;
    private int level;
}
