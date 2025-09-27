package org.arsw.maze_rush.users.repository;

import java.util.UUID;

import org.arsw.maze_rush.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
	Optional<UserEntity> findByUsernameIgnoreCase(String username);
	Optional<UserEntity> findByEmailIgnoreCase(String email);
	boolean existsByUsernameIgnoreCase(String username);
	boolean existsByEmailIgnoreCase(String email);
}
