package org.arsw.maze_rush.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.List;

import org.arsw.maze_rush.users.dto.UpdateProfileRequestDTO;
import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // REGISTER
    @Test
    void testRegisterUser() throws Exception {

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("sebastian");

        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"sebastian\",\"email\":\"test@example.com\",\"password\":\"12345678\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("sebastian"));
    }

    // GET CURRENT USER
    @Test
    void testGetCurrentUser_OK() throws Exception {

        Principal principal = () -> "sebastian";

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("sebastian");

        when(userService.findUserByUsername("sebastian"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sebastian"));
    }

    @Test
    void testGetCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // LIST USERS
    @Test
    void testListUsers() throws Exception {

        UserResponseDTO u = new UserResponseDTO();
        u.setUsername("testUser");

        when(userService.findAllUsers())
                .thenReturn(List.of(u));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testUser"));
    }

    //  GET BY USERNAME
    @Test
    void testGetByUsername() throws Exception {

        UserResponseDTO u = new UserResponseDTO();
        u.setUsername("john");

        when(userService.findUserByUsername("john"))
                .thenReturn(u);

        mockMvc.perform(get("/api/v1/users/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    // GET BY EMAIL
    @Test
    void testGetByEmail() throws Exception {

        UserResponseDTO u = new UserResponseDTO();
        u.setUsername("mailuser");

        when(userService.findUserByEmail("mail@test.com"))
                .thenReturn(u);

        mockMvc.perform(get("/api/v1/users/by-email")
                .param("email", "mail@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mailuser"));
    }

    // UPDATE CURRENT USER
    @Test
    void testUpdateCurrentUser_OK() throws Exception {

        Principal principal = () -> "sebastian";

        UserResponseDTO updated = new UserResponseDTO();
        updated.setUsername("sebastian");

        when(userService.updateProfile(eq("sebastian"), any(UpdateProfileRequestDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/me")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"newemail@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sebastian"));
    }

    @Test
    void testUpdateCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"xd@test.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    //  UPDATE USER 
    @Test
    void testUpdateUser() throws Exception {

        UserResponseDTO updated = new UserResponseDTO();
        updated.setUsername("johndoe");

        when(userService.updateUser(eq("johndoe"), any(UserRequestDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/johndoe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"john\",\"email\":\"xd@test.com\",\"password\":\"12345678\"}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    // DELETE USER
    @Test
    void testDeleteUser() throws Exception {

        mockMvc.perform(delete("/api/v1/users/john"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("john");
    }
}
