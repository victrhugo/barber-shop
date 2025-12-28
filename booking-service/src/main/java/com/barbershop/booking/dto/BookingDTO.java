package com.barbershop.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private String id;
    private String userId;
    private String clientName;
    private String barberId;
    private String barberName;
    private ServiceDTO service;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}



