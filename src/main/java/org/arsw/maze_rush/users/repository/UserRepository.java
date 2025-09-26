package org.arsw.maze_rush.users.repository;

import java.util.UUID;

import org.arsw.maze_rush.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
}
