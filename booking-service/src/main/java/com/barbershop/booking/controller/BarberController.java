package com.barbershop.booking.controller;

import com.barbershop.booking.dto.CreateBarberRequest;
import com.barbershop.booking.dto.UpdateBarberRequest;
import com.barbershop.booking.entity.Barber;
import com.barbershop.booking.repository.BarberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
    
    @PersistenceContext
    private EntityManager entityManager;
    
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
    
    @GetMapping("/admin/all")
    public ResponseEntity<List<BarberDTO>> getAllBarbersForAdmin() {
        List<Barber> barbers = barberRepository.findAll();
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
            log.info("Received request to create barber: userId={}, bio={}, specialties={}", 
                request.getUserId(), request.getBio(), request.getSpecialties());
            
            // Check if barber already exists for this user
            var existingBarber = barberRepository.findByUserId(request.getUserId());
            if (existingBarber.isPresent()) {
                log.info("Barber already exists for userId: {}, returning existing barber", request.getUserId());
                BarberDTO existingDTO = BarberDTO.builder()
                        .id(existingBarber.get().getId().toString())
                        .userId(existingBarber.get().getUserId().toString())
                        .specialties(existingBarber.get().getSpecialties())
                        .bio(existingBarber.get().getBio())
                        .rating(existingBarber.get().getRating())
                        .active(existingBarber.get().getActive())
                        .build();
                
                UserInfo userInfo = getUserInfo(existingBarber.get().getUserId());
                existingDTO.setFullName(userInfo != null ? userInfo.getFullName() : null);
                existingDTO.setEmail(userInfo != null ? userInfo.getEmail() : null);
                
                return ResponseEntity.ok(existingDTO);
            }
            
            // Create barber using native SQL to avoid foreign key constraint issues
            // This approach inserts directly into the database, bypassing JPA transaction isolation
            // We don't check if user exists first - we try to insert and handle foreign key errors with retry
            log.info("Attempting to create barber for userId={} (will retry on foreign key errors)", request.getUserId());
            Barber barber = createBarberWithNativeSQL(request.getUserId(), request.getBio(), request.getSpecialties());
            
            if (barber == null) {
                log.error("Failed to create barber using native SQL: userId={}", request.getUserId());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Não foi possível criar o barbeiro. Tente novamente em alguns instantes."));
            }
            
            log.info("Barber created successfully using native SQL: barberId={}, userId={}", 
                barber.getId(), barber.getUserId());

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
            
            log.info("Barber DTO created: id={}, fullName={}, email={}", 
                barberDTO.getId(), barberDTO.getFullName(), barberDTO.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(barberDTO);
        } catch (Exception e) {
            log.error("Error creating barber: userId={}", request.getUserId(), e);
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

    private Barber createBarberWithNativeSQL(UUID userId, String bio, String[] specialties) {
        log.info("=== createBarberWithNativeSQL called: userId={}, bio={}, specialties={} ===", 
            userId, bio, specialties != null ? java.util.Arrays.toString(specialties) : "null");
        
        int maxRetries = 20;
        int retryDelayMs = 500;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Barber result = attemptCreateBarber(userId, bio, specialties, attempt);
                if (result != null) {
                    return result;
                }
                
                // If result is null, it means foreign key constraint failed, retry
                if (attempt < maxRetries) {
                    log.info("Retrying after {}ms delay: userId={}, attempt={}", 
                        retryDelayMs * attempt, userId, attempt + 1);
                    Thread.sleep(retryDelayMs * attempt);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupted during retry delay: userId={}", userId);
                break;
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                if (errorMsg.contains("foreign key") || errorMsg.contains("barbers_user_id_fkey")) {
                    log.warn("Foreign key error: userId={}, attempt={}/{}", userId, attempt, maxRetries);
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelayMs * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        return null;
                    }
                } else {
                    log.error("Unexpected error creating barber: userId={}, attempt={}", userId, attempt, e);
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelayMs * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
        
        log.error("Failed to create barber after {} attempts: userId={}", maxRetries, userId);
        return null;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Barber attemptCreateBarber(UUID userId, String bio, String[] specialties, int attempt) {
        try {
            log.info("Attempt {}: Creating barber with native SQL: userId={}", attempt, userId);
            
            // Clear Hibernate cache to see latest database state
            entityManager.clear();
            
            // Check if barber already exists
            var existingBarber = barberRepository.findByUserId(userId);
            if (existingBarber.isPresent()) {
                log.info("Barber already exists for userId={}, returning existing: barberId={}", 
                    userId, existingBarber.get().getId());
                return existingBarber.get();
            }
            
            // Use native SQL to insert barber directly
            // This bypasses JPA transaction isolation issues
            String specialtiesArray = specialties != null && specialties.length > 0 
                ? "ARRAY[" + java.util.Arrays.stream(specialties)
                    .map(s -> "'" + s.replace("'", "''") + "'")
                    .collect(java.util.stream.Collectors.joining(",")) + "]"
                : "NULL";
            
            String sql = String.format(
                "INSERT INTO barbers (id, user_id, bio, specialties, active, rating, created_at, updated_at) " +
                "VALUES (gen_random_uuid(), '%s', %s, %s, true, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (user_id) DO NOTHING " +
                "RETURNING id",
                userId,
                bio != null ? "'" + bio.replace("'", "''") + "'" : "NULL",
                specialtiesArray
            );
            
            log.debug("Executing SQL: {}", sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
            
            if (results.isEmpty()) {
                // Barber already exists (ON CONFLICT DO NOTHING)
                log.info("Barber insert returned no results (likely already exists), checking database: userId={}", userId);
                var barber = barberRepository.findByUserId(userId);
                if (barber.isPresent()) {
                    log.info("Found existing barber: barberId={}, userId={}", barber.get().getId(), userId);
                    return barber.get();
                }
                log.warn("No barber found after insert attempt: userId={}, attempt={}", userId, attempt);
                return null; // Will trigger retry
            } else {
                UUID barberId = UUID.fromString(results.get(0)[0].toString());
                log.info("✅ Barber created successfully with native SQL: barberId={}, userId={}, attempt={}", 
                    barberId, userId, attempt);
                
                // Load the created barber entity
                var createdBarber = barberRepository.findById(barberId);
                if (createdBarber.isPresent()) {
                    return createdBarber.get();
                }
                return null;
            }
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            String causeMsg = e.getCause() != null && e.getCause().getMessage() != null ? e.getCause().getMessage() : "";
            boolean isForeignKeyError = errorMsg.contains("foreign key constraint") || 
                                      errorMsg.contains("barbers_user_id_fkey") ||
                                      causeMsg.contains("foreign key constraint") ||
                                      causeMsg.contains("barbers_user_id_fkey");
            
            if (isForeignKeyError) {
                log.warn("Foreign key constraint violation (user may not be committed yet): userId={}, attempt={}, error={}", 
                    userId, attempt, errorMsg);
                return null; // Will trigger retry
            } else {
                log.error("Unexpected constraint violation: userId={}, attempt={}, error={}", userId, attempt, errorMsg, e);
                throw e;
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errorMsg.contains("foreign key") || errorMsg.contains("barbers_user_id_fkey")) {
                log.warn("Foreign key error caught as generic exception: userId={}, attempt={}", userId, attempt);
                return null; // Will trigger retry
            } else {
                log.error("Unexpected error creating barber: userId={}, attempt={}", userId, attempt, e);
                throw e;
            }
        }
    }
    
    private UserInfo getUserInfo(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.debug("Fetching user info from: {}", url);
            UserInfo userInfo = restTemplate.getForObject(url, UserInfo.class);
            log.debug("User info retrieved: fullName={}, email={}", 
                userInfo != null ? userInfo.getFullName() : "null",
                userInfo != null ? userInfo.getEmail() : "null");
            return userInfo;
        } catch (Exception e) {
            log.warn("Error fetching user info for userId: {} from {}. Error: {}", 
                userId, userServiceUrl, e.getMessage());
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


