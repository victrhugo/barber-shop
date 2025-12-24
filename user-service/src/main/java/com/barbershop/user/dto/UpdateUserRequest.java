package com.barbershop.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Telefone inválido")
    private String phone;
}



