package org.arsw.maze_rush.auth.util;

import org.arsw.maze_rush.auth.dto.AuthResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;

public class AuthResponseFactory {

    private AuthResponseFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static AuthResponseDTO build(
            UserEntity user,
            String accessToken,
            String refreshToken,
            long expirationSeconds
    ) {
        AuthResponseDTO.UserInfo userInfo = AuthResponseDTO.UserInfo.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .score(user.getScore())
                .level(user.getLevel())
                .build();

        return AuthResponseDTO.builder()
                .user(userInfo)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expirationSeconds)
                .build();
    }
}

