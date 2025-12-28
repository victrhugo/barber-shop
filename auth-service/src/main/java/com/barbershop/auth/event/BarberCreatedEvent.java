package com.barbershop.auth.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberCreatedEvent {
    private UUID userId;
    private String bio;
    private String[] specialties;
}

