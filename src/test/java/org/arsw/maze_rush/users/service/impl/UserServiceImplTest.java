package org.arsw.maze_rush.users.service.impl;

import org.arsw.maze_rush.common.exceptions.ConflictException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UpdateProfileRequestDTO;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl service;

    UserEntity mockUser;

    @BeforeEach
    void setup() {
        mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("olduser");
        mockUser.setEmail("old@mail.com");
        mockUser.setScore(10);
        mockUser.setLevel(1);
        mockUser.setBio("old bio");
        mockUser.setAvatarColor("#000");
        mockUser.setPreferredMazeSize("15x15");
    }

    // findAllUsers()
    @Test
    void testFindAllUsers_ok() {
        UserEntity u1 = new UserEntity();
        u1.setId(UUID.randomUUID());
        u1.setUsername("user1");
        u1.setEmail("u1@mail.com");

        UserEntity u2 = new UserEntity();
        u2.setId(UUID.randomUUID());
        u2.setUsername("user2");
        u2.setEmail("u2@mail.com");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponseDTO> result = service.findAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }


    // findUserByUsername()
    @Test
    void testFindUserByUsername_ok() {
        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        UserResponseDTO dto = service.findUserByUsername("olduser");

        assertEquals("olduser", dto.getUsername());
    }

    @Test
    void testFindUserByUsername_notFound() {
        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findUserByUsername("ghost"));
    }

    // findUserByEmail()
    @Test
    void testFindUserByEmail_ok() {
        when(userRepository.findByEmailIgnoreCase("old@mail.com"))
                .thenReturn(Optional.of(mockUser));

        UserResponseDTO dto = service.findUserByEmail("old@mail.com");

        assertEquals("old@mail.com", dto.getEmail());
    }

    @Test
    void testFindUserByEmail_notFound() {
        when(userRepository.findByEmailIgnoreCase("x@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findUserByEmail("x@mail.com"));
    }

    // createUser()
    @Test
    void testCreateUser_ok() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("newUser");
        req.setEmail("new@mail.com");
        req.setPassword("123");

        when(userRepository.existsByUsernameIgnoreCase("newUser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(false);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO dto = service.createUser(req);

        assertEquals("newUser", dto.getUsername());
    }

    @Test
    void testCreateUser_usernameConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("taken");
        req.setEmail("x@mail.com");

        when(userRepository.existsByUsernameIgnoreCase("taken")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(req));
    }

    @Test
    void testCreateUser_emailConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("new");
        req.setEmail("taken@mail.com");

        when(userRepository.existsByUsernameIgnoreCase("new")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("taken@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(req));
    }


    // updateUser() 
    @Test
    void testUpdateUser_ok() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("updated");
        req.setEmail("updated@mail.com");
        req.setPassword("pass");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByUsernameIgnoreCase("updated")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("updated@mail.com")).thenReturn(false);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO dto = service.updateUser("olduser", req);

        assertEquals("updated", dto.getUsername());
    }

    @Test
    void testUpdateUser_notFound() {
        when(userRepository.findByUsernameIgnoreCase("x"))
                .thenReturn(Optional.empty());
        UserRequestDTO requestDto = new UserRequestDTO();
        assertThrows(NotFoundException.class, () -> 
            service.updateUser("x", requestDto)
        );
    }

    @Test
    void testUpdateUser_usernameConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("taken");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByUsernameIgnoreCase("taken")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateUser("olduser", req));
    }

    @Test
    void testUpdateUser_emailConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("taken@mail.com");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByEmailIgnoreCase("taken@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateUser("olduser", req));
    }


    // deleteUser()
    @Test
    void testDeleteUser_ok() {
        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        assertDoesNotThrow(() -> service.deleteUser("olduser"));
        verify(userRepository).delete(mockUser);
    }

    @Test
    void testDeleteUser_notFound() {
        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.deleteUser("ghost"));
    }

    // updateProfile()
    @Test
    void testUpdateProfile_success() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();
        req.setUsername("NewUser");
        req.setEmail("NEW@MAIL.COM");
        req.setBio("  new bio  ");
        req.setAvatarColor("  #FFF  ");
        req.setPreferredMazeSize("  20x20 ");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByUsernameIgnoreCase("NewUser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.updateProfile("olduser", req);

        assertEquals("NewUser", result.getUsername());
        assertEquals("new@mail.com", result.getEmail());
        assertEquals("new bio", result.getBio());
        assertEquals("#FFF", result.getAvatarColor());
        assertEquals("20x20", result.getPreferredMazeSize());
    }

    @Test
    void testUpdateProfile_usernameConflict() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();
        req.setUsername("taken");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByUsernameIgnoreCase("taken")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateProfile("olduser", req));
    }

    @Test
    void testUpdateProfile_emailConflict() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();
        req.setEmail("taken@mail.com");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByEmailIgnoreCase("taken@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateProfile("olduser", req));
    }

    @Test
    void testUpdateProfile_noChanges() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.updateProfile("olduser", req);

        assertEquals("olduser", result.getUsername());
        assertEquals("old@mail.com", result.getEmail());
    }

    @Test
    void testUpdateProfile_updateOnlyBio() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();
        req.setBio(" changed ");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.updateProfile("olduser", req);

        assertEquals("changed", result.getBio());
        assertEquals("olduser", result.getUsername());
    }

    @Test
    void testUpdateProfile_trimmedValues() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();
        req.setUsername("   newname   ");
        req.setEmail("  new@mail.com   ");

        when(userRepository.findByUsernameIgnoreCase("olduser"))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.existsByUsernameIgnoreCase("newname")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@mail.com")).thenReturn(false);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.updateProfile("olduser", req);

        assertEquals("newname", result.getUsername());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    void testUpdateProfile_userNotFound() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO();

        when(userRepository.findByUsernameIgnoreCase("x"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.updateProfile("x", req));
    }
}
