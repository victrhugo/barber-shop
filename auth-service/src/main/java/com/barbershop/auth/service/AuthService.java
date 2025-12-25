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
import org.springframework.transaction.annotation.Transactional;
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

        // Create barber entry in booking-service
        try {
            CreateBarberInBookingServiceRequest barberRequest = new CreateBarberInBookingServiceRequest();
            barberRequest.setUserId(user.getId());
            barberRequest.setBio(request.getBio());
            barberRequest.setSpecialties(request.getSpecialties());
            
            String url = bookingServiceUrl + "/api/barbers";
            restTemplate.postForEntity(url, barberRequest, Object.class);
            log.info("Barber entry created in booking-service for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create barber entry in booking-service for user: {}", user.getId(), e);
            // Don't fail user creation, but log the error
        }

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return user;
    }
}



