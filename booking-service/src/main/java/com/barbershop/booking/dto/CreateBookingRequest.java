package com.barbershop.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateBookingRequest {
    
    @NotNull(message = "Serviço é obrigatório")
    private String serviceId;
    
    @NotNull(message = "Data é obrigatória")
    @Future(message = "Data deve ser futura")
    private LocalDate bookingDate;
    
    @NotNull(message = "Horário é obrigatório")
    private LocalTime bookingTime;
    
    private String barberId; // Optional - if null, system assigns automatically
    
    private String notes;
}



