package com.barbershop.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("BarberShop - Confirme seu Email");
            message.setText(buildVerificationEmailBody(token));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private String buildVerificationEmailBody(String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        
        return String.format("""
            Bem-vindo à BarberShop!
            
            Por favor, confirme seu email clicando no link abaixo:
            
            %s
            
            Este link expirará em 24 horas.
            
            Se você não criou uma conta, por favor ignore este email.
            
            Atenciosamente,
            Equipe BarberShop
            """, verificationLink);
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("BarberShop - Conta Criada com Sucesso!");
            message.setText(buildWelcomeEmailBody(fullName));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    private String buildWelcomeEmailBody(String fullName) {
        return String.format("""
            Olá %s,
            
            Sua conta na BarberShop foi criada com sucesso!
            
            Estamos muito felizes em tê-lo conosco. Agora você pode:
            - Agendar seus cortes e serviços de barbearia
            - Gerenciar seus agendamentos
            - Acompanhar seu histórico
            
            Não esqueça de verificar seu email para ativar sua conta completamente.
            
            Se tiver alguma dúvida, não hesite em entrar em contato conosco.
            
            Bem-vindo à BarberShop!
            
            Atenciosamente,
            Equipe BarberShop
            """, fullName);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("BarberShop - Recuperação de Senha");
            message.setText(buildPasswordResetEmailBody(token));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    private String buildPasswordResetEmailBody(String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        return String.format("""
            Olá,
            
            Recebemos uma solicitação para redefinir sua senha.
            
            Clique no link abaixo para criar uma nova senha:
            
            %s
            
            Este link expirará em 1 hora.
            
            Se você não solicitou a redefinição de senha, por favor ignore este email.
            
            Atenciosamente,
            Equipe BarberShop
            """, resetLink);
    }
}



