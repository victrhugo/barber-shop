package com.barbershop.auth.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateBarberInBookingServiceRequest {
    private UUID userId;
    private String bio;
    private String[] specialties;
}

