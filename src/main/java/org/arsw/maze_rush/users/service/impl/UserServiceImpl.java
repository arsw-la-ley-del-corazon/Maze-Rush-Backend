package org.arsw.maze_rush.users.service.impl;

import java.util.List;

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

    // CONSULTAS
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByUsername(String username) {
        UserEntity entity = findByUsernameOrThrow(username);
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));
        return toResponse(entity);
    }

    // CREACIÓN
    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO user) {

        String username = normalize(user.getUsername());
        String email = normalizeEmail(user.getEmail());

        ensureUsernameAvailable(username);
        ensureEmailAvailable(email);

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));

        return toResponse(userRepository.save(entity));
    }
    // UPDATE USER (ADMIN)
    @Override
    @Transactional
    public UserResponseDTO updateUser(String username, UserRequestDTO user) {
        UserEntity entity = findByUsernameOrThrow(username);

        applyUsernameChange(entity, user.getUsername());
        applyEmailChange(entity, user.getEmail());

        if (isPresent(user.getPassword())) {
            entity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return toResponse(userRepository.save(entity));
    }

    // DELETE
    @Override
    @Transactional
    public void deleteUser(String username) {
        UserEntity entity = findByUsernameOrThrow(username);
        userRepository.delete(entity);
    }

    // UPDATE PROFILE (PLAYER EDITS THEIR PROFILE)
    @Override
    @Transactional
    public UserResponseDTO updateProfile(String username, UpdateProfileRequestDTO data) {

        UserEntity entity = findByUsernameOrThrow(username);

        // Validaciones
        validateProfileChanges(entity, data);

        // Aplicar actualizaciones
        applyProfileChanges(entity, data);

        return toResponse(userRepository.save(entity));
    }

    // VALIDATIONS
    private void validateProfileChanges(UserEntity entity, UpdateProfileRequestDTO data) {

        if (isUpdated(data.getUsername(), entity.getUsername())) {
            ensureUsernameAvailable(data.getUsername().trim());
        }

        if (isUpdated(data.getEmail(), entity.getEmail())) {
            ensureEmailAvailable(data.getEmail().trim());
        }
    }

    private void ensureUsernameAvailable(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("El username ya está en uso");
        }
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("El email ya está en uso");
        }
    }

    private boolean isUpdated(String newValue, String oldValue) {
        return newValue != null && !newValue.isBlank() && !newValue.equalsIgnoreCase(oldValue);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

  
    // APPLY CHANGES
    private void applyProfileChanges(UserEntity entity, UpdateProfileRequestDTO data) {

        applyUsernameChange(entity, data.getUsername());
        applyEmailChange(entity, data.getEmail());

        if (data.getBio() != null) {
            entity.setBio(data.getBio().trim());
        }

        if (isPresent(data.getAvatarColor())) {
            entity.setAvatarColor(data.getAvatarColor().trim());
        }

        if (isPresent(data.getPreferredMazeSize())) {
            entity.setPreferredMazeSize(data.getPreferredMazeSize().trim());
        }
    }

    private void applyUsernameChange(UserEntity entity, String newUsername) {
        if (isUpdated(newUsername, entity.getUsername())) {
            ensureUsernameAvailable(newUsername.trim());
            entity.setUsername(newUsername.trim());
        }
    }

    private void applyEmailChange(UserEntity entity, String newEmail) {
        if (isUpdated(newEmail, entity.getEmail())) {
            ensureEmailAvailable(newEmail.trim().toLowerCase());
            entity.setEmail(newEmail.trim().toLowerCase());
        }
    }

    // HELPERS
    private UserEntity findByUsernameOrThrow(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + username));
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase();
    }

    // DTO MAPPING
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
