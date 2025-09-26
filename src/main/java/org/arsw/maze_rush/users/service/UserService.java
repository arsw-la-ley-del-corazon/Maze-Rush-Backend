package org.arsw.maze_rush.users.service;

import java.util.List;

import org.arsw.maze_rush.users.dto.UserResponseDTO;

public interface  UserService {
    List<UserResponseDTO> findAllUsers();
    UserResponseDTO findUserByUsername(String username);
    UserResponseDTO findUserByEmail(String email);
    UserResponseDTO createUser(UserResponseDTO user);
    UserResponseDTO updateUser(String username, UserResponseDTO user);
    void deleteUser(String username);
    
}
