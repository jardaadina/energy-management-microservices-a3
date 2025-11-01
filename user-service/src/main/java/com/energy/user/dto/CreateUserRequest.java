package com.energy.user.dto;

import com.energy.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String address;
    private User.Role role;
}