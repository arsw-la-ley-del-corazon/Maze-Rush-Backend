package org.arsw.maze_rush.users.repository;

import java.util.Optional;
import java.util.UUID;

import org.arsw.maze_rush.users.entities.PasswordResetToken;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar tokens de recuperación de contraseña
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Busca un token por su valor
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Elimina todos los tokens de un usuario
     */
    void deleteByUser(UserEntity user);
    
    /**
     * Elimina tokens expirados (limpieza periódica)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
    
    /**
     * Cuenta tokens activos para un usuario
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user = :user AND t.used = false AND t.expiryDate > CURRENT_TIMESTAMP")
    long countActiveTokensByUser(@Param("user") UserEntity user);
}
