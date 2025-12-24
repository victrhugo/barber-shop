package com.barbershop.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}



