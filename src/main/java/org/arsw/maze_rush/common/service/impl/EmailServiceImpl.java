package org.arsw.maze_rush.common.service.impl;

import org.arsw.maze_rush.common.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación simulada del servicio de email.
 * En modo desarrollo, imprime el token en consola.
 * En producción, se debería implementar con JavaMailSender o un servicio como SendGrid.
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("📧 SIMULACIÓN DE EMAIL - RECUPERACIÓN DE CONTRASEÑA");
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Para: {}", to);
        logger.info("Asunto: Recuperación de Contraseña - Maze Rush");
        logger.info("───────────────────────────────────────────────────────────");
        logger.info("Hola,");
        logger.info("");
        logger.info("Has solicitado restablecer tu contraseña de Maze Rush.");
        logger.info("");
        logger.info("🔑 Tu token de recuperación es:");
        logger.info("");
        logger.info("    {}", token);
        logger.info("");
        logger.info("Este token es válido por 1 hora.");
        logger.info("");
        logger.info("Si no solicitaste este cambio, ignora este mensaje.");
        logger.info("");
        logger.info("Saludos,");
        logger.info("El equipo de Maze Rush");
        logger.info("═══════════════════════════════════════════════════════════");
        
        // TODO: En producción, reemplazar con envío real de email
        // Ejemplo con JavaMailSender:
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true);
        // helper.setTo(to);
        // helper.setSubject("Recuperación de Contraseña - Maze Rush");
        // helper.setText(emailBody, true);
        // mailSender.send(message);
    }
}
