package com.energy.authentication.dto;

import com.energy.authentication.entity.User;
import lombok.Data;

@Data
public class InternalCreateUserRequest {
    private String username;
    private String password;
    private User.Role role;
    private Long userId;
}