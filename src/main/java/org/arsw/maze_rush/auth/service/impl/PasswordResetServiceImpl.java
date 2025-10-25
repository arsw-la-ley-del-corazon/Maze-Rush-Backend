package org.arsw.maze_rush.auth.service.impl;

import java.security.SecureRandom;
import java.util.Base64;

import org.arsw.maze_rush.auth.dto.ForgotPasswordRequestDTO;
import org.arsw.maze_rush.auth.dto.ResetPasswordRequestDTO;
import org.arsw.maze_rush.auth.service.PasswordResetService;
import org.arsw.maze_rush.common.exceptions.BadRequestException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.common.service.EmailService;
import org.arsw.maze_rush.users.entities.AuthProvider;
import org.arsw.maze_rush.users.entities.PasswordResetToken;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.PasswordResetTokenRepository;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de recuperación de contraseñas
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int TOKEN_LENGTH = 32; // 32 bytes = 256 bits
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        
        // Buscar usuario
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("No existe una cuenta con ese email"));
        
        // Validar que sea usuario local (no OAuth2)
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            logger.warn("Intento de recuperación de contraseña para usuario OAuth2: {}", email);
            throw new BadRequestException(
                "Esta cuenta usa autenticación con " + user.getAuthProvider() + 
                ". No puedes cambiar la contraseña aquí."
            );
        }
        
        // Limpiar tokens anteriores del usuario (invalidarlos)
        tokenRepository.deleteByUser(user);
        
        // Generar nuevo token seguro
        String token = generateSecureToken();
        
        // Crear y guardar token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        tokenRepository.save(resetToken);
        
        // Enviar email con el token
        emailService.sendPasswordResetEmail(email, token);
        
        logger.info("Token de recuperación generado para usuario: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        String token = request.getToken().trim();
        
        // Buscar token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido o expirado"));
        
        // Validar token
        if (!resetToken.isValid()) {
            logger.warn("Intento de usar token inválido o expirado: {}", token);
            throw new BadRequestException("El token ha expirado o ya fue utilizado");
        }
        
        // Obtener usuario
        UserEntity user = resetToken.getUser();
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        logger.info("Contraseña actualizada exitosamente para usuario: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }
    
    /**
     * Genera un token seguro usando SecureRandom y Base64
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
