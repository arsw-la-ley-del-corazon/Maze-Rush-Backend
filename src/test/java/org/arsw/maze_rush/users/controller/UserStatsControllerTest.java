package org.arsw.maze_rush.users.controller;

import org.arsw.maze_rush.auth.filter.JwtAuthenticationFilter;
import org.arsw.maze_rush.auth.handler.OAuth2AuthenticationSuccessHandler;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.CookieUtil;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.common.exceptions.NotFoundException;
import org.arsw.maze_rush.users.dto.UserStatsDTO;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.arsw.maze_rush.users.service.UserStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; 
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserStatsController.class)
@AutoConfigureMockMvc(addFilters = false) 
class UserStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserStatsService userStatsService;
    
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


    @Test
    void getUserStats_ShouldReturn200AndData() throws Exception {
        String username = "pro_player";
        UserStatsDTO mockStats = UserStatsDTO.builder()
                .username(username)
                .gamesPlayed(10)
                .gamesWon(5)
                .winRate(50.0)
                .fastestTimeMs(12000L)
                .build();

        when(userStatsService.getStats(username)).thenReturn(mockStats);
        mockMvc.perform(get("/api/v1/users/{username}/stats", username)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.gamesPlayed").value(10))
                .andExpect(jsonPath("$.gamesWon").value(5))
                .andExpect(jsonPath("$.winRate").value(50.0))
                .andExpect(jsonPath("$.fastestTimeMs").value(12000));

        verify(userStatsService).getStats(username);
    }
    
    @Test
    void getUserStats_WhenUserNotFound_ShouldReturn404() throws Exception {
        when(userStatsService.getStats("unknown"))
             .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/unknown/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() != 404) {
                        Exception resolved = result.getResolvedException();
                        if (resolved == null) {
                            throw new AssertionError("Se esperaba un error 404 o una excepción, pero fue " + result.getResponse().getStatus());
                        }
                        if (!(resolved instanceof NotFoundException)) {
                            throw new AssertionError("Se esperaba NotFoundException, pero fue " + resolved.getClass().getName());
                        }
                    }
                });
    }
}