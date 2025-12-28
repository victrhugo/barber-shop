package com.barbershop.booking.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateBarberRequest {
    private UUID userId;
    private String bio;
    private String[] specialties;
}




