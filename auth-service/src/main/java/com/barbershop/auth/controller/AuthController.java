package com.barbershop.auth.controller;

import com.barbershop.auth.dto.AuthResponse;
import com.barbershop.auth.dto.LoginRequest;
import com.barbershop.auth.dto.RegisterRequest;
import com.barbershop.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("message", "Email verificado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "Email de verificação reenviado"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/barbers")
    public ResponseEntity<?> createBarber(@Valid @RequestBody com.barbershop.auth.dto.CreateBarberRequest request) {
        try {
            com.barbershop.auth.entity.User barber = authService.createBarber(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", barber.getId().toString(),
                "email", barber.getEmail(),
                "fullName", barber.getFullName(),
                "role", barber.getRole().name(),
                "message", "Barbeiro criado com sucesso"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Temporary endpoint to reset admin password - REMOVE IN PRODUCTION
    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetAdminPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String newPassword = body.get("newPassword");
            
            if (email == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email e newPassword são obrigatórios"));
            }
            
            authService.resetPassword(email, newPassword);
            return ResponseEntity.ok(Map.of("message", "Senha atualizada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

