package com.energy.user.dto;

import com.energy.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegistrationRequest {
    private String username;
    private String password;
    private User.Role role;
    private Long userId;
}