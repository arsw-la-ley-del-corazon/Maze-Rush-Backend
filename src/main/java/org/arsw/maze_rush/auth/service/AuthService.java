package org.arsw.maze_rush.auth.service;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.auth.dto.LoginRequestDTO;
import org.arsw.maze_rush.auth.dto.RefreshTokenRequestDTO;
import org.arsw.maze_rush.users.dto.UserRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(UserRequestDTO request);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    void logout(String token);
    boolean validateToken(String token);
}