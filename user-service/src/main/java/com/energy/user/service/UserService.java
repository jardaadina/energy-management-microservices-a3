package com.energy.user.service;

import com.energy.user.dto.CreateUserRequest;
import com.energy.user.dto.UpdateUserRequest;
import com.energy.user.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserRequest request);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}