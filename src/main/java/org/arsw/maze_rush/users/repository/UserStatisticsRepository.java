package org.arsw.maze_rush.users.repository;

import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.entities.UserStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatisticsEntity, UUID> {
    Optional<UserStatisticsEntity> findByUser(UserEntity user);
}