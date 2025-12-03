package org.arsw.maze_rush.common.exceptions;

public class LobbyFullException extends RuntimeException {
    public LobbyFullException(String message) {
        super(message);
    }
}
