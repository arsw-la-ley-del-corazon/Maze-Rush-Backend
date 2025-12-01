package org.arsw.maze_rush.auth.service;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;

public interface AuthService {
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    void logout(String token);
    boolean validateToken(String token);
}