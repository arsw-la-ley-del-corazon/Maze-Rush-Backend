package org.arsw.maze_rush.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar recuperación de contraseña
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    private String email;
}
