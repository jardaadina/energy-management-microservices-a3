package com.energy.user.service.impl;

import com.energy.user.dto.CreateUserRequest;
import com.energy.user.dto.UpdateUserRequest;
import com.energy.user.dto.UserDTO;
import com.energy.user.entity.User;
import com.energy.user.exception.ResourceAlreadyExistsException;
import com.energy.user.exception.ResourceNotFoundException;
import com.energy.user.mapper.UserMapper;
import com.energy.user.repository.UserRepository;
import com.energy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${device.service.url:http://localhost:8082}")
    private String deviceServiceBaseUrl;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("An user with this email already exists: " + request.getEmail());
        }

        User user = UserMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("User created with id: {}", savedUser.getId());
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
        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {}
        {
            if(userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("An user with this email already exists: " + request.getEmail());
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

        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }
}