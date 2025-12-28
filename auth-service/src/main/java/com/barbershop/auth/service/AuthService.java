package com.barbershop.auth.service;

import com.barbershop.auth.dto.AuthResponse;
import com.barbershop.auth.dto.CreateBarberRequest;
import com.barbershop.auth.dto.CreateBarberInBookingServiceRequest;
import com.barbershop.auth.dto.LoginRequest;
import com.barbershop.auth.dto.RegisterRequest;
import com.barbershop.auth.entity.User;
import com.barbershop.auth.repository.UserRepository;
import com.barbershop.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${booking.service.url:http://localhost:8083}")
    private String bookingServiceUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.USER)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        user = userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        // Generate token
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        // Generate token
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email já verificado");
        }

        // Generate new token
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send email
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public User createBarber(CreateBarberRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Create user with BARBER role
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.BARBER)
                .emailVerified(true) // Admin creates barbers, so email is automatically verified
                .build();

        user = userRepository.save(user);
        // Force flush to ensure user is persisted in database
        userRepository.flush();
        
        log.info("User created and flushed: userId={}, email={}", user.getId(), user.getEmail());
        
        // Publish event to create barber entry in booking-service AFTER transaction commits
        eventPublisher.publishEvent(new com.barbershop.auth.event.BarberCreatedEvent(
            user.getId(), 
            request.getBio(), 
            request.getSpecialties()
        ));
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return user;
    }
    
    // Event listener to create barber entry in booking-service AFTER transaction commits
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBarberCreatedEvent(com.barbershop.auth.event.BarberCreatedEvent event) {
        log.info("BarberCreatedEvent received after transaction commit: userId={}, bio={}, specialties={}", 
            event.getUserId(), event.getBio(), event.getSpecialties());
        
        // Small delay to ensure database replication/visibility
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted during delay", e);
        }
        
        // Create barber entry in booking-service with retry
        CreateBarberInBookingServiceRequest barberRequest = new CreateBarberInBookingServiceRequest();
        barberRequest.setUserId(event.getUserId());
        barberRequest.setBio(event.getBio());
        barberRequest.setSpecialties(event.getSpecialties());
        
        String url = bookingServiceUrl + "/api/barbers";
        log.info("Creating barber entry in booking-service: url={}, userId={}, bio={}, specialties={}", 
            url, event.getUserId(), event.getBio(), event.getSpecialties());
        
        int maxRetries = 3;
        int retryDelayMs = 1000; // 1 second
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                org.springframework.http.ResponseEntity<?> response = restTemplate.postForEntity(url, barberRequest, Object.class);
                log.info("✅ Barber entry created in booking-service for user: {}, response status: {}, attempt: {}", 
                    event.getUserId(), response.getStatusCode(), attempt);
                return; // Success, exit method
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                lastException = e;
                log.warn("HTTP error creating barber entry in booking-service for user: {}. Status: {}, Response: {}, attempt: {}/{}", 
                    event.getUserId(), e.getStatusCode(), e.getResponseBodyAsString(), attempt, maxRetries);
                
                // If it's a 4xx error (client error), retry anyway (might be foreign key constraint)
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting to retry barber creation", ie);
                        return;
                    }
                } else {
                    log.error("Failed to create barber entry in booking-service after {} attempts: userId={}, error={}", 
                        maxRetries, event.getUserId(), e.getResponseBodyAsString());
                    // Don't throw exception - this is async, we don't want to break the transaction
                    return;
                }
            } catch (org.springframework.web.client.ResourceAccessException e) {
                lastException = e;
                log.warn("Connection error creating barber entry in booking-service for user: {}. Error: {}, attempt: {}/{}", 
                    event.getUserId(), e.getMessage(), attempt, maxRetries);
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting to retry barber creation", ie);
                        return;
                    }
                } else {
                    log.error("Failed to connect to booking-service after {} attempts: userId={}", 
                        maxRetries, event.getUserId());
                    return;
                }
            } catch (Exception e) {
                lastException = e;
                log.error("Unexpected error creating barber entry in booking-service for user: {}. URL: {}. Error: {}, attempt: {}/{}", 
                    event.getUserId(), url, e.getMessage(), attempt, maxRetries);
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting to retry barber creation", ie);
                        return;
                    }
                } else {
                    log.error("Failed to create barber entry in booking-service after {} attempts: userId={}", 
                        maxRetries, event.getUserId());
                    return;
                }
            }
        }
    }
}



