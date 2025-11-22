package org.arsw.maze_rush.users.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.arsw.maze_rush.common.exceptions.ConflictException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UpdateProfileRequestDTO;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.arsw.maze_rush.users.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "Usuario no encontrado: ";


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByUsername(String username) {
        UserEntity entity = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO user) {
        // Normalizar y validar unicidad
        String username = user.getUsername().trim();
        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("El username ya está en uso");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El email ya está en uso");
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        // score=0, level=1 por defecto
        entity = userRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(String username, UserRequestDTO user) {
        UserEntity entity = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));

        // Actualización parcial: username/email si cambian y no colisionan
        if (user.getUsername() != null && !user.getUsername().isBlank() &&
            !entity.getUsername().equalsIgnoreCase(user.getUsername().trim())) {
            String newUsername = user.getUsername().trim();
            if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                throw new ConflictException("El username ya está en uso");
            }
            entity.setUsername(newUsername);
        }

        if (user.getEmail() != null && !user.getEmail().isBlank() &&
            !entity.getEmail().equalsIgnoreCase(user.getEmail().trim())) {
            String newEmail = user.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new ConflictException("El email ya está en uso");
            }
            entity.setEmail(newEmail);
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        entity = userRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        UserEntity entity = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));
        userRepository.delete(entity);
    }

    @Override
    @Transactional
    public UserResponseDTO updateProfile(String username, UpdateProfileRequestDTO profileData) {
        UserEntity entity = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));

        // Actualizar username si se proporcionó
        if (profileData.getUsername() != null && !profileData.getUsername().isBlank()) {
            String newUsername = profileData.getUsername().trim();
            if (!entity.getUsername().equalsIgnoreCase(newUsername)) {
                if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                    throw new ConflictException("El username ya está en uso");
                }
                entity.setUsername(newUsername);
            }
        }

        // Actualizar email si se proporcionó
        if (profileData.getEmail() != null && !profileData.getEmail().isBlank()) {
            String newEmail = profileData.getEmail().trim().toLowerCase();
            if (!entity.getEmail().equalsIgnoreCase(newEmail)) {
                if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                    throw new ConflictException("El email ya está en uso");
                }
                entity.setEmail(newEmail);
            }
        }

        // Actualizar bio
        if (profileData.getBio() != null) {
            entity.setBio(profileData.getBio().trim());
        }

        // Actualizar avatarColor
        if (profileData.getAvatarColor() != null && !profileData.getAvatarColor().isBlank()) {
            entity.setAvatarColor(profileData.getAvatarColor().trim());
        }

        // Actualizar preferredMazeSize
        if (profileData.getPreferredMazeSize() != null && !profileData.getPreferredMazeSize().isBlank()) {
            entity.setPreferredMazeSize(profileData.getPreferredMazeSize().trim());
        }

        entity = userRepository.save(entity);
        return toResponse(entity);
    }

    private UserResponseDTO toResponse(UserEntity entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setScore(entity.getScore());
        dto.setLevel(entity.getLevel());
        dto.setBio(entity.getBio());
        dto.setAvatarColor(entity.getAvatarColor());
        dto.setPreferredMazeSize(entity.getPreferredMazeSize());
        return dto;
    }
}
