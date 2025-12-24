package com.barbershop.booking.controller;

import com.barbershop.booking.entity.Barber;
import com.barbershop.booking.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberRepository barberRepository;

    @GetMapping
    public ResponseEntity<List<BarberDTO>> getAllBarbers() {
        List<Barber> barbers = barberRepository.findByActiveTrue();
        List<BarberDTO> barberDTOs = barbers.stream()
                .map(barber -> BarberDTO.builder()
                        .id(barber.getId().toString())
                        .userId(barber.getUserId().toString())
                        .specialties(barber.getSpecialties())
                        .bio(barber.getBio())
                        .rating(barber.getRating())
                        .active(barber.getActive())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(barberDTOs);
    }

    @GetMapping("/{barberId}")
    public ResponseEntity<BarberDTO> getBarberById(@PathVariable String barberId) {
        return barberRepository.findById(UUID.fromString(barberId))
                .map(barber -> ResponseEntity.ok(BarberDTO.builder()
                        .id(barber.getId().toString())
                        .userId(barber.getUserId().toString())
                        .specialties(barber.getSpecialties())
                        .bio(barber.getBio())
                        .rating(barber.getRating())
                        .active(barber.getActive())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BarberDTO {
        private String id;
        private String userId;
        private String[] specialties;
        private String bio;
        private java.math.BigDecimal rating;
        private Boolean active;
    }
}


