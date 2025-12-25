package com.barbershop.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${user.service.url:http://localhost:8080}")
    private String userServiceUrl;

    @Async
    public void sendBookingConfirmationEmail(UUID userId, String serviceName, String bookingDate, String bookingTime) {
        try {
            // Get user email from user-service
            String userEmail = getUserEmail(userId);
            String userName = getUserName(userId);
            
            if (userEmail == null) {
                log.warn("Could not send booking email: user email not found for userId: {}", userId);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("BarberShop - Agendamento Confirmado!");
            message.setText(buildBookingConfirmationEmailBody(userName, serviceName, bookingDate, bookingTime));
            
            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to user: {}", userId, e);
        }
    }

    private String buildBookingConfirmationEmailBody(String userName, String serviceName, String bookingDate, String bookingTime) {
        return String.format("""
            Olá %s,
            
            Seu agendamento foi confirmado com sucesso!
            
            Detalhes do agendamento:
            - Serviço: %s
            - Data: %s
            - Horário: %s
            
            Aguardamos você na data e horário agendados.
            
            Se precisar cancelar ou remarcar, entre em contato conosco.
            
            Atenciosamente,
            Equipe BarberShop
            """, userName != null ? userName : "Cliente", serviceName, bookingDate, bookingTime);
    }

    private String getUserEmail(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            UserInfo userInfo = restTemplate.getForObject(url, UserInfo.class);
            return userInfo != null ? userInfo.getEmail() : null;
        } catch (Exception e) {
            log.error("Error fetching user email for userId: {}", userId, e);
            return null;
        }
    }

    private String getUserName(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            UserInfo userInfo = restTemplate.getForObject(url, UserInfo.class);
            return userInfo != null ? userInfo.getFullName() : null;
        } catch (Exception e) {
            log.error("Error fetching user name for userId: {}", userId, e);
            return null;
        }
    }

    // DTO for user info
    private static class UserInfo {
        private String email;
        private String fullName;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}

