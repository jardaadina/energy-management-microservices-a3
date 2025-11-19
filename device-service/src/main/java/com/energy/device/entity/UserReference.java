package com.energy.device.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_reference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReference {

    @Id
    private Long userId;

    @Column(nullable = false)
    private String username;
}