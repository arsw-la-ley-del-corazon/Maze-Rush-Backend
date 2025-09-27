package org.arsw.maze_rush.users.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.arsw.maze_rush.common.exceptions.ConflictException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.common.exceptions.UnauthorizedException;
import org.arsw.maze_rush.users.dto.LoginRequestDTO;
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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByUsername(String username) {
        UserEntity entity = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
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
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));

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
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
        userRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO login(LoginRequestDTO request) {
        UserEntity entity = null;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            entity = userRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));
        } else if (request.getUsername() != null && !request.getUsername().isBlank()) {
            entity = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));
        } else {
            throw new UnauthorizedException("Debe proporcionar email o username");
        }

        if (!passwordEncoder.matches(request.getPassword(), entity.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        return toResponse(entity);
    }

    private UserResponseDTO toResponse(UserEntity entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setScore(entity.getScore());
        dto.setLevel(entity.getLevel());
        return dto;
    }
}
