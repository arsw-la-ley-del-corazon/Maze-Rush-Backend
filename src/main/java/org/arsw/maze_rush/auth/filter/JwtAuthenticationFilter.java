package org.arsw.maze_rush.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arsw.maze_rush.auth.service.AuthService;
import org.arsw.maze_rush.auth.util.JwtUtil;
import org.arsw.maze_rush.users.entities.UserEntity;
import org.arsw.maze_rush.users.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            username = jwtUtil.getUsernameFromToken(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Validar token usando el servicio de autenticación (incluye blacklist)
            if (authService.validateToken(jwt)) {
                // Crear UserDetails mínimo para Spring Security
                UserDetails userDetails = createUserDetails(username);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private UserDetails createUserDetails(String username) {
        // Buscar usuario en la base de datos para validar que aún existe
        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(username);
        
        if (userOpt.isEmpty()) {
            // Usuario no existe, retornar UserDetails básico que falla la autenticación
            return User.builder()
                    .username(username)
                    .password("")
                    .authorities(new ArrayList<>())
                    .disabled(true)
                    .build();
        }

        // Retornar UserDetails válido
        return User.builder()
                .username(username)
                .password("") // No exponemos la contraseña
                .authorities(new ArrayList<>()) // Por ahora sin roles específicos
                .build();
    }
}