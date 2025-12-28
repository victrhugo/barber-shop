package com.barbershop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBarberRequest {
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;
    
    @NotBlank(message = "Nome completo é obrigatório")
    private String fullName;
    
    private String phone;
    
    private String bio;
    
    private String[] specialties;
    
    // Optional: allows admin to create barbers with ADMIN role (shop owner)
    private String role; // "BARBER" or "ADMIN"
}



