package org.arsw.maze_rush.maze.controller;

import org.arsw.maze_rush.auth.filter.JwtAuthenticationFilter;
import org.arsw.maze_rush.auth.handler.OAuth2AuthenticationSuccessHandler;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.maze.entities.MazeEntity;
import org.arsw.maze_rush.maze.service.MazeService;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*; 
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MazeController.class)
@AutoConfigureMockMvc(addFilters = false)
class MazeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MazeService mazeService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private AuthService authService;
    @MockitoBean  
    private UserRepository userRepository;
    @MockitoBean
    private CookieUtil cookieUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @MockitoBean
    private CorsConfigurationSource corsConfigurationSource;

    private MazeEntity mockMaze;
    private final UUID mazeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMaze = new MazeEntity();
        mockMaze.setId(mazeId);
        mockMaze.setSize("MEDIUM");
        mockMaze.setWidth(20);
        mockMaze.setHeight(20);
        mockMaze.setLayout("[[1, 1, 1], [1, 0, 1], [1, 1, 1]]");
    }

    @Test
    void testGenerateMaze_ShouldReturn200AndMaze() throws Exception {
        String sizeParam = "MEDIUM";
        when(mazeService.generateMaze(sizeParam)).thenReturn(mockMaze);
        mockMvc.perform(post("/api/v1/map/generate/{size}", sizeParam)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(mazeId.toString()))
                .andExpect(jsonPath("$.size").value("MEDIUM"))
                .andExpect(jsonPath("$.width").value(20));

        verify(mazeService, times(1)).generateMaze(sizeParam);
    }

    @Test
    void testGetMazeById_ShouldReturn200AndMaze() throws Exception {
        when(mazeService.getMazeById(mazeId)).thenReturn(mockMaze);
        mockMvc.perform(get("/api/v1/map/{id}", mazeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mazeId.toString()))
                .andExpect(jsonPath("$.layout").exists());

        verify(mazeService, times(1)).getMazeById(mazeId);
    }
    
    @Test
    void testGetMazeById_NotFound_ShouldPropagateException() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(mazeService.getMazeById(randomId)).thenThrow(new RuntimeException("Maze not found"));

        mockMvc.perform(get("/api/v1/map/{id}", randomId)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isInternalServerError()) 
                .andExpect(result -> {
                    Throwable exception = result.getResolvedException();
                    assertNotNull(exception, "Debería haber una excepción resuelta");
                    assertTrue(exception instanceof RuntimeException);
                    assertEquals("Maze not found", exception.getMessage());
                });
    }
}