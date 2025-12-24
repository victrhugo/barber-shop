package com.barbershop.booking.controller;

import com.barbershop.booking.dto.ServiceDTO;
import com.barbershop.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        try {
            List<ServiceDTO> services = bookingService.getAllServices();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable String serviceId) {
        try {
            ServiceDTO service = bookingService.getServiceById(UUID.fromString(serviceId));
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

