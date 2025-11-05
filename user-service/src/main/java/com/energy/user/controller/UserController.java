package com.energy.user.controller;

import com.energy.user.dto.CreateUserRequest;
import com.energy.user.dto.UpdateUserRequest;
import com.energy.user.dto.UserDTO;
import com.energy.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private void checkAdmin(String role)
    {
        if (!"ADMIN".equalsIgnoreCase(role))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied: Requires ADMIN role.");
        }
    }

    @Operation(summary = "Creează un utilizator nou", description = "Creează un utilizator nou (client sau admin). Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilizator creat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide (ex. username/parola lipsă)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Utilizatorul există deja (Conflict)", content = @Content)
    })

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request,
                                              @RequestHeader("X-User-Role") String role)
    {
        checkAdmin(role);
        log.info("REST request to create user: {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Obține un utilizator după ID", description = "Returnează un singur utilizator, pe baza ID-ului. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator găsit",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizator negăsit", content = @Content)
    })

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
                                               @RequestHeader("X-User-Role") String role)
    {
        checkAdmin(role);
        log.info("REST request to get user: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @Operation(summary = "Obține un utilizator după username", description = "Returnează un singur utilizator, pe baza numelui de utilizator. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator găsit",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizator negăsit", content = @Content)
    })

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username,
                                                     @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get user by username: {}", username);
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Obține toți utilizatorii", description = "Returnează o listă cu toți utilizatorii din sistem. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista utilizatorilor",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content)
    })

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        log.info("REST request to get all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Actualizează un utilizator", description = "Actualizează datele unui utilizator (fără parolă). Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator actualizat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizator negăsit", content = @Content)
    })

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to update user: {}", id);
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Șterge un utilizator", description = "Șterge un utilizator din sistem. Necesită rol ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilizator șters cu succes", content = @Content),
            @ApiResponse(responseCode = "401", description = "Acces refuzat (Rolul nu este ADMIN)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizator negăsit", content = @Content)
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to delete user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}