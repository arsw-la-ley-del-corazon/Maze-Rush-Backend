package org.arsw.maze_rush.auth.controller;

import org.arsw.maze_rush.auth.dto.ForgotPasswordRequestDTO;
import org.arsw.maze_rush.auth.dto.MessageResponseDTO;
import org.arsw.maze_rush.auth.dto.ResetPasswordRequestDTO;
import org.arsw.maze_rush.auth.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador para recuperación de contraseñas
 */
@RestController
@RequestMapping("/api/v1/auth/password")
@Tag(name = "Password Reset", description = "Endpoints para recuperación de contraseña")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    @Operation(summary = "Solicitar recuperación de contraseña", 
               description = "Genera un token de recuperación y lo envía al email del usuario (simulado en consola)")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(new MessageResponseDTO(
            "Se ha enviado un email con las instrucciones para recuperar tu contraseña. " +
            "Revisa la consola del servidor para obtener el token."
        ));
    }

    @PostMapping("/reset")
    @Operation(summary = "Resetear contraseña", 
               description = "Cambia la contraseña usando el token de recuperación")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO(
            "Tu contraseña ha sido actualizada exitosamente"
        ));
    }
}
