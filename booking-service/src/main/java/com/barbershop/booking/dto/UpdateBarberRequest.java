package com.barbershop.booking.dto;

import lombok.Data;

@Data
public class UpdateBarberRequest {
    private String bio;
    private String[] specialties;
    private Boolean active;
}



