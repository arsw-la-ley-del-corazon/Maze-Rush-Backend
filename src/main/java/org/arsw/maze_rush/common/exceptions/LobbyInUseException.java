package org.arsw.maze_rush.common.exceptions;

public class LobbyInUseException extends RuntimeException {
    public LobbyInUseException(String message) {
        super(message);
    }
}
