package com.energy.authentication.service;

import com.energy.authentication.dto.*;
import com.energy.authentication.entity.User;
import com.energy.authentication.repository.UserRepository;
import com.energy.authentication.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                String.valueOf(user.getRole()),
                user.getUserId()
        );

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getUserId()
        );
    }

    public ValidateTokenResponse validateToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            return new ValidateTokenResponse(true, username, role, userId);
        }

        return new ValidateTokenResponse(false, null, null, null);
    }

    public Map<String, String> validateTokenForTraefik(String token) {
        log.debug("Validating token for Traefik");

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token received");
            throw new RuntimeException("Invalid token");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        log.info("Token validated successfully for user: {}", username);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", String.valueOf(userId));
        headers.put("X-User-Role", role);
        headers.put("X-Username", username);

        return headers;
    }

    public void registerNewUser(String username, String password, User.Role role, Long userId) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User credentials already exist for: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setUserId(userId);

        userRepository.save(user);
    }

    public void deleteUserCredentials(Long userId) {
        log.info("Attempting to delete credentials for user ID: {}", userId);
        userRepository.deleteByUserId(userId);
        log.info("Successfully deleted credentials (if they existed) for user ID: {}", userId);
    }
}