package com.energy.user.service.impl;

import com.energy.user.dto.*;
import com.energy.user.entity.User;
import com.energy.user.exception.ResourceAlreadyExistsException;
import com.energy.user.exception.ResourceNotFoundException;
import com.energy.user.mapper.UserMapper;
import com.energy.user.repository.UserRepository;
import com.energy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${device.service.url:http://localhost:8082}")
    private String deviceServiceBaseUrl;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    @Value("${rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest createUserRequest) {
        userRepository.findByUsername(createUserRequest.getUsername()).ifPresent(s -> {
            throw new ResourceAlreadyExistsException("User already exists with username: " + createUserRequest.getUsername());
        });

        User user = User.builder()
                .username(createUserRequest.getUsername())
                .name(createUserRequest.getName())
                .email(createUserRequest.getEmail())
                .age(createUserRequest.getAge())
                .address(createUserRequest.getAddress())
                .role(createUserRequest.getRole())
                .build();

        User savedUser = userRepository.save(user);


        String authServiceUrl = "http://auth-service:8083/auth/internal/register";

        AuthRegistrationRequest authRequest = new AuthRegistrationRequest(
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                createUserRequest.getRole(),
                savedUser.getId()
        );

        try {
            restTemplate.postForEntity(authServiceUrl, authRequest, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user credentials in auth-service. Rolling back.", e);
        }

        try {
            SyncEvent syncEvent = SyncEvent.builder()
                    .eventType("USER_CREATED")
                    .entityId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .build();

            rabbitTemplate.convertAndSend(
                    syncExchange,
                    userCreatedRoutingKey,
                    syncEvent
            );

            log.info("Published USER_CREATED sync event for user: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish sync event: {}", e.getMessage());
        }

        return UserMapper.toDTO(savedUser);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
            }
        }
        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new ResourceAlreadyExistsException("An user with this email already exists: " + request.getEmail());
                }
            }
        }
        UserMapper.updateEntity(user, request);
        User updatedUser = userRepository.save(user);

        log.info("User updated with id: {}", updatedUser.getId());
        return UserMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        try {
            log.info("Requesting deletion of device assignments for user id: {}", id);
            String deleteUrl = deviceServiceBaseUrl + "/user-devices/by-user/" + id;
            restTemplate.delete(deleteUrl);

            log.info("Device assignments deletion successful for user id: {}", id);

        } catch (Exception e) {
            log.error("Failed to delete device assignments for user id: {}. Error: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete device assignments, rolling back user deletion", e);
        }
        try {
            String authServiceUrl = "http://auth-service:8083/auth/internal/delete/" + id;
            log.info("Requesting deletion of auth credentials for user id: {}", id);
            restTemplate.delete(authServiceUrl);
            log.info("Auth credentials deletion successful for user id: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete auth credentials for user id: {}. Error: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete auth credentials, rolling back user deletion", e);
        }

        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }
}