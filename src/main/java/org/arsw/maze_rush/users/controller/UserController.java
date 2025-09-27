package org.arsw.maze_rush.users.controller;

import java.security.Principal;
import java.util.List;

import org.arsw.maze_rush.users.dto.UserRequestDTO;
import org.arsw.maze_rush.users.dto.UserResponseDTO;
import org.arsw.maze_rush.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Registro (ahora manejado por AuthController, pero se mantiene para compatibilidad)
    @PostMapping
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        // Nota: Este endpoint podría ser deprecated en favor de /api/v1/auth/register
        UserResponseDTO created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Obtener perfil del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.findUserByUsername(principal.getName()));
    }

    // Listado
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    // Consulta por username
    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findUserByUsername(username));
    }

    // Consulta por email
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDTO> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    // Actualización parcial del usuario autenticado
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(Principal principal,
                                                             @Valid @RequestBody UserRequestDTO request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateUser(principal.getName(), request));
    }

    // Actualización parcial por username (admin only - para futuro)
    @PatchMapping("/{username}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String username,
                                                      @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    // Borrado por username
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
