package com.energy.user.controller;

import com.energy.user.dto.CreateUserRequest;
import com.energy.user.dto.UpdateUserRequest;
import com.energy.user.dto.UserDTO;
import com.energy.user.service.UserService;
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

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request,
                                              @RequestHeader("X-User-Role") String role)
    {
        checkAdmin(role);
        log.info("REST request to create user: {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
                                               @RequestHeader("X-User-Role") String role)
    {
        checkAdmin(role);
        log.info("REST request to get user: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username,
                                                     @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to get user by username: {}", username);
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        log.info("REST request to get all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader("X-User-Role") String role)
    {   checkAdmin(role);
        log.info("REST request to delete user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}