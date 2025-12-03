package org.arsw.maze_rush.users.service;

import java.util.List;

import org.arsw.maze_rush.users.dto.UpdateProfileRequestDTO;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;

public interface  UserService {
    List<UserResponseDTO> findAllUsers();
    UserResponseDTO findUserByUsername(String username);
    UserResponseDTO findUserByEmail(String email);
    UserResponseDTO createUser(UserRequestDTO user);
    UserResponseDTO updateUser(String username, UserRequestDTO user);
    UserResponseDTO updateProfile(String username, UpdateProfileRequestDTO profileData);
    void deleteUser(String username);
    
}
