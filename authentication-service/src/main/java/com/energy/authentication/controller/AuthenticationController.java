package com.energy.authentication.controller;

import com.energy.authentication.dto.*;
import com.energy.authentication.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;


import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Autentificare utilizator", description = "Autentifica un utilizator folosind username si parola si returneaza un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autentificare reușită",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Date de autentificare invalide",
                    content = @Content)
    })

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "Validare Token pentru Traefik", description = "Endpoint special pentru API Gateway (Traefik) pentru a valida token-ul din header-ul Authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token valid. Header-ele X-User-Id, X-User-Name, X-User-Role sunt returnate."),
            @ApiResponse(responseCode = "401", description = "Token invalid, expirat sau lipsește header-ul Authorization")
    })

    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(@RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = authenticationService.validateToken(request.getToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validare Token pentru Traefik", description = "Endpoint special pentru API Gateway (Traefik) pentru a valida token-ul din header-ul Authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token valid. Header-ele X-User-Id, X-User-Name, X-User-Role sunt returnate."),
            @ApiResponse(responseCode = "401", description = "Token invalid, expirat sau lipsește header-ul Authorization")
    })

    @GetMapping("/validate")
    public ResponseEntity<?> validateTokenForTraefik(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String token = authHeader.substring(7);
            Map<String, String> headers = authenticationService.validateTokenForTraefik(token);

            HttpHeaders responseHeaders = new HttpHeaders();
            headers.forEach(responseHeaders::set);

            return ResponseEntity.ok().headers(responseHeaders).build();

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Health Check", description = "Verifică starea de funcționare a serviciului.")
    @ApiResponse(responseCode = "200", description = "Serviciul rulează")

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication Service is running");
    }

    @Operation(summary = "Înregistrare internă", description = "Endpoint intern, apelat de 'user-service' la crearea unui utilizator nou pentru a crea și credențialele.")
    @ApiResponse(responseCode = "200", description = "Credențiale create")

    @PostMapping("/internal/register")
    public ResponseEntity<String> internalRegisterUser(@RequestBody InternalCreateUserRequest request) {
        authenticationService.registerNewUser(request.getUsername(), request.getPassword(), request.getRole(), request.getUserId());
        return ResponseEntity.ok("User credentials created successfully.");
    }

    @Operation(summary = "Ștergere internă", description = "Endpoint intern, apelat de 'user-service' la ștergerea unui utilizator pentru a șterge și credențialele.")
    @ApiResponse(responseCode = "200", description = "Credențiale șterse")

    @DeleteMapping("/internal/delete/{userId}")
    public ResponseEntity<String> internalDeleteUser(@PathVariable Long userId) {
        authenticationService.deleteUserCredentials(userId);
        return ResponseEntity.ok("User credentials deleted successfully.");
    }
}