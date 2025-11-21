package org.arsw.maze_rush.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir información del usuario desde Google OAuth2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfoDTO {
    private String sub; // Google User ID
    private String name;
    private String givenName;
    private String familyName;
    private String email;
    private Boolean emailVerified;
    private String locale;
}
