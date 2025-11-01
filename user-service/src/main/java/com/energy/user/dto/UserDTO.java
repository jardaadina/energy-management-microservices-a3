package com.energy.user.dto;

import com.energy.user.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String address;
    private User.Role role;
}