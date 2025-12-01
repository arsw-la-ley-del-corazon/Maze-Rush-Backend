package org.arsw.maze_rush.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

import org.arsw.maze_rush.common.ApiError;
import org.arsw.maze_rush.users.dto.UpdateProfileRequestDTO;
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
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Crear nuevo usuario",
        description = "Registra un nuevo usuario (endpoint de compatibilidad - usar preferiblemente /api/v1/auth/register)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Usuario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "El username o email ya están en uso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> register(
        @Parameter(description = "Datos del usuario a crear", required = true)
        @Valid @RequestBody UserRequestDTO request
    ) {
        UserResponseDTO created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Obtener perfil del usuario autenticado",
        description = "Retorna la información del usuario actualmente autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Perfil del usuario obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Usuario no autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.findUserByUsername(principal.getName()));
    }

    @Operation(
        summary = "Listar todos los usuarios",
        description = "Obtiene una lista de todos los usuarios registrados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class))
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @Operation(
        summary = "Obtener usuario por username",
        description = "Busca un usuario específico por su nombre de usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getByUsername(
        @Parameter(description = "Nombre de usuario a buscar", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        return ResponseEntity.ok(userService.findUserByUsername(username));
    }

    @Operation(
        summary = "Obtener usuario por email",
        description = "Busca un usuario específico por su dirección de correo electrónico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Formato de email inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDTO> getByEmail(
        @Parameter(description = "Email del usuario a buscar", required = true, example = "user@example.com")
        @RequestParam String email
    ) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @Operation(
        summary = "Actualizar perfil del usuario autenticado",
        description = "Permite al usuario autenticado actualizar parcialmente su información personal"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Perfil actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Usuario no autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "El username o email ya están en uso por otro usuario",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
        Principal principal,
        @Parameter(description = "Datos a actualizar (campos opcionales)", required = true)
        @Valid @RequestBody UpdateProfileRequestDTO request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }

    @Operation(
        summary = "Actualizar usuario por username",
        description = "Actualiza parcialmente la información de un usuario específico (funcionalidad administrativa)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "El username o email ya están en uso por otro usuario",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{username}")
    public ResponseEntity<UserResponseDTO> updateUser(
        @Parameter(description = "Nombre de usuario a actualizar", required = true, example = "johndoe")
        @PathVariable String username,
        @Parameter(description = "Datos a actualizar (campos opcionales)", required = true)
        @Valid @RequestBody UserRequestDTO request
    ) {
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina permanentemente un usuario del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Usuario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Nombre de usuario a eliminar", required = true, example = "johndoe")
        @PathVariable String username
    ) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
