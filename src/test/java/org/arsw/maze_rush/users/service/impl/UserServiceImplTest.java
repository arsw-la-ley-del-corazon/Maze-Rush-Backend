package org.arsw.maze_rush.users.service.impl;

import org.arsw.maze_rush.common.exceptions.ConflictException;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl service;

    private UserEntity userEntity;

    @BeforeEach
    void setup() {
        userEntity = new UserEntity();
        userEntity.setId(java.util.UUID.randomUUID());
        userEntity.setUsername("TestUser");
        userEntity.setEmail("test@mail.com");
        userEntity.setPassword("123");
        userEntity.setScore(10);
        userEntity.setLevel(1);
    }

    // findAllUsers
    @Test
    void testFindAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        List<UserResponseDTO> result = service.findAllUsers();

        assertEquals(1, result.size());
        assertEquals("TestUser", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    // findUserByUsername 
    @Test
    void testFindUserByUsername() {
        when(userRepository.findByUsernameIgnoreCase("TestUser"))
                .thenReturn(Optional.of(userEntity));

        UserResponseDTO dto = service.findUserByUsername("TestUser");

        assertEquals("TestUser", dto.getUsername());
    }

    // findUserByUsername not found
    @Test
    void testFindUserByUsername_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("ABC"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findUserByUsername("ABC"));
    }


    // findUserByEmail 
    @Test
    void testFindUserByEmail() {
        when(userRepository.findByEmailIgnoreCase("test@mail.com"))
                .thenReturn(Optional.of(userEntity));

        UserResponseDTO dto = service.findUserByEmail("test@mail.com");

        assertEquals("TestUser", dto.getUsername());
    }

    // findUserByEmail not found
    @Test
    void testFindUserByEmail_NotFound() {
        when(userRepository.findByEmailIgnoreCase("no@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findUserByEmail("no@mail.com"));
    }

    // createUser 
    @Test
    void testCreateUser_OK() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("Nuevo");
        req.setPassword("1234");
        req.setEmail("nuevo@mail.com");

        when(userRepository.existsByUsernameIgnoreCase("Nuevo")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("nuevo@mail.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserResponseDTO dto = service.createUser(req);

        assertEquals("TestUser", dto.getUsername()); // from saved entity mock
    }

    // username conflict
    @Test
    void testCreateUser_UsernameConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("Nuevo");
        req.setEmail("nuevo@mail.com");

        when(userRepository.existsByUsernameIgnoreCase("Nuevo")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(req));
    }

    // email conflict
    @Test
    void testCreateUser_EmailConflict() {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("Nuevo");
        req.setEmail("nuevo@mail.com");

        when(userRepository.existsByUsernameIgnoreCase("Nuevo")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("nuevo@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(req));
    }

    // updateUser 
    @Test
    void testUpdateUser_OK() {
        when(userRepository.findByUsernameIgnoreCase("TestUser"))
                .thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("NuevoNombre");
        req.setEmail("nuevo@mail.com");
        req.setPassword("123");

        when(userRepository.existsByUsernameIgnoreCase("NuevoNombre")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("nuevo@mail.com")).thenReturn(false);

        UserResponseDTO dto = service.updateUser("TestUser", req);

        assertEquals("NuevoNombre", dto.getUsername());
    }

    // updateUser user not found
    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("X"))
                .thenReturn(Optional.empty());

        UserRequestDTO req = new UserRequestDTO();
        assertThrows(NotFoundException.class, () -> service.updateUser("X", req));
    }

    // username already used
    @Test
    void testUpdateUser_UsernameConflict() {
        when(userRepository.findByUsernameIgnoreCase("TestUser"))
                .thenReturn(Optional.of(userEntity));

        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("Otro");

        when(userRepository.existsByUsernameIgnoreCase("Otro")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateUser("TestUser", req));
    }

    // email already used
    @Test
    void testUpdateUser_EmailConflict() {
        when(userRepository.findByUsernameIgnoreCase("TestUser"))
                .thenReturn(Optional.of(userEntity));

        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("otro@mail.com");

        when(userRepository.existsByEmailIgnoreCase("otro@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.updateUser("TestUser", req));
    }

    // deleteUser 
    @Test
    void testDeleteUser() {
        when(userRepository.findByUsernameIgnoreCase("TestUser"))
                .thenReturn(Optional.of(userEntity));

        assertDoesNotThrow(() -> service.deleteUser("TestUser"));
        verify(userRepository).delete(userEntity);
    }

    // deleteUser not found
    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("X"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.deleteUser("X"));
    }

}
