package org.arsw.maze_rush.game.logic.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arsw.maze_rush.users.entities.UserEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPosition {
    private UserEntity player;
    private int x;
    private int y;
    private int score = 0;
}
