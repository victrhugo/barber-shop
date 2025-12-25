package com.barbershop.booking.controller;

import com.barbershop.booking.dto.CreateBarberRequest;
import com.barbershop.booking.dto.UpdateBarberRequest;
import com.barbershop.booking.entity.Barber;
import com.barbershop.booking.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
@Slf4j
public class BarberController {

    private final BarberRepository barberRepository;
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;

    @GetMapping
    public ResponseEntity<List<BarberDTO>> getAllBarbers() {
        List<Barber> barbers = barberRepository.findByActiveTrue();
        List<BarberDTO> barberDTOs = barbers.stream()
                .map(barber -> {
                    UserInfo userInfo = getUserInfo(barber.getUserId());
                    return BarberDTO.builder()
                            .id(barber.getId().toString())
                            .userId(barber.getUserId().toString())
                            .fullName(userInfo != null ? userInfo.getFullName() : null)
                            .email(userInfo != null ? userInfo.getEmail() : null)
                            .specialties(barber.getSpecialties())
                            .bio(barber.getBio())
                            .rating(barber.getRating())
                            .active(barber.getActive())
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(barberDTOs);
    }

    @GetMapping("/{barberId}")
    public ResponseEntity<BarberDTO> getBarberById(@PathVariable String barberId) {
        return barberRepository.findById(UUID.fromString(barberId))
                .map(barber -> {
                    UserInfo userInfo = getUserInfo(barber.getUserId());
                    return ResponseEntity.ok(BarberDTO.builder()
                            .id(barber.getId().toString())
                            .userId(barber.getUserId().toString())
                            .fullName(userInfo != null ? userInfo.getFullName() : null)
                            .email(userInfo != null ? userInfo.getEmail() : null)
                            .specialties(barber.getSpecialties())
                            .bio(barber.getBio())
                            .rating(barber.getRating())
                            .active(barber.getActive())
                            .build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBarber(@RequestBody CreateBarberRequest request) {
        try {
            // Check if barber already exists for this user
            if (barberRepository.findByUserId(request.getUserId()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Barbeiro já existe para este usuário"));
            }

            Barber barber = Barber.builder()
                    .userId(request.getUserId())
                    .bio(request.getBio())
                    .specialties(request.getSpecialties())
                    .active(true)
                    .build();

            barber = barberRepository.save(barber);

            BarberDTO barberDTO = BarberDTO.builder()
                    .id(barber.getId().toString())
                    .userId(barber.getUserId().toString())
                    .specialties(barber.getSpecialties())
                    .bio(barber.getBio())
                    .rating(barber.getRating())
                    .active(barber.getActive())
                    .build();

            UserInfo userInfo = getUserInfo(barber.getUserId());
            barberDTO.setFullName(userInfo != null ? userInfo.getFullName() : null);
            barberDTO.setEmail(userInfo != null ? userInfo.getEmail() : null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(barberDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{barberId}")
    public ResponseEntity<?> updateBarber(
            @PathVariable String barberId,
            @RequestBody UpdateBarberRequest request) {
        try {
            Barber barber = barberRepository.findById(UUID.fromString(barberId))
                    .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado"));

            if (request.getBio() != null) {
                barber.setBio(request.getBio());
            }
            if (request.getSpecialties() != null) {
                barber.setSpecialties(request.getSpecialties());
            }
            if (request.getActive() != null) {
                barber.setActive(request.getActive());
            }

            barber = barberRepository.save(barber);

            UserInfo userInfo = getUserInfo(barber.getUserId());
            BarberDTO barberDTO = BarberDTO.builder()
                    .id(barber.getId().toString())
                    .userId(barber.getUserId().toString())
                    .fullName(userInfo != null ? userInfo.getFullName() : null)
                    .email(userInfo != null ? userInfo.getEmail() : null)
                    .specialties(barber.getSpecialties())
                    .bio(barber.getBio())
                    .rating(barber.getRating())
                    .active(barber.getActive())
                    .build();

            return ResponseEntity.ok(barberDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating barber", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erro ao atualizar barbeiro"));
        }
    }

    @DeleteMapping("/{barberId}")
    public ResponseEntity<?> deleteBarber(@PathVariable String barberId) {
        try {
            Barber barber = barberRepository.findById(UUID.fromString(barberId))
                    .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado"));

            // Soft delete - set active to false
            barber.setActive(false);
            barberRepository.save(barber);

            return ResponseEntity.ok(Map.of("message", "Barbeiro desativado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting barber", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erro ao desativar barbeiro"));
        }
    }

    private UserInfo getUserInfo(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            return restTemplate.getForObject(url, UserInfo.class);
        } catch (Exception e) {
            log.error("Error fetching user info for userId: {}", userId, e);
            return null;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BarberDTO {
        private String id;
        private String userId;
        private String fullName;
        private String email;
        private String[] specialties;
        private String bio;
        private java.math.BigDecimal rating;
        private Boolean active;
    }

    @lombok.Data
    private static class UserInfo {
        private String email;
        private String fullName;
    }
}


