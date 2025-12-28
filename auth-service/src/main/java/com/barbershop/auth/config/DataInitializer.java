package com.barbershop.auth.config;

import com.barbershop.auth.entity.User;
import com.barbershop.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeAdmin();
    }

    private void initializeAdmin() {
        String adminEmail = "admin@barbershop.com";
        String adminPassword = "admin123"; // Default password - should be changed in production
        
        // Check if admin already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ Admin user already exists: {}", adminEmail);
            return;
        }

        // Create default admin user
        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("Administrator")
                .role(User.Role.ADMIN)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin user created: {} (password: {})", adminEmail, adminPassword);
        log.warn("⚠️  IMPORTANT: Change the default admin password in production!");
    }
}


