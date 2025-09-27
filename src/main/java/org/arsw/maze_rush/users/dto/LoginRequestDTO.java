package org.arsw.maze_rush.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    // Permitir login con email o username; si viene email, ignora username
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;
}
