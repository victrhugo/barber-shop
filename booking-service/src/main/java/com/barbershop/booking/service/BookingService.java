package com.barbershop.booking.service;

import com.barbershop.booking.dto.BookingDTO;
import com.barbershop.booking.dto.CreateBookingRequest;
import com.barbershop.booking.dto.ServiceDTO;
import com.barbershop.booking.entity.Barber;
import com.barbershop.booking.entity.Booking;
import com.barbershop.booking.entity.Service;
import com.barbershop.booking.repository.BarberRepository;
import com.barbershop.booking.repository.BookingRepository;
import com.barbershop.booking.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("🔧 BookingService initialized with userServiceUrl: {}", userServiceUrl);
    }

    // Service Management
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findByActiveTrue().stream()
                .map(this::mapServiceToDTO)
                .collect(Collectors.toList());
    }

    public ServiceDTO getServiceById(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        return mapServiceToDTO(service);
    }

    // Booking Management
    @Transactional
    public BookingDTO createBooking(UUID userId, CreateBookingRequest request) {
        log.info("Creating booking for userId={}, serviceId={}, date={}, time={}", 
            userId, request.getServiceId(), request.getBookingDate(), request.getBookingTime());
        
        // Validate service exists
        Service service = serviceRepository.findById(UUID.fromString(request.getServiceId()))
                .orElseThrow(() -> {
                    log.error("Service not found: {}", request.getServiceId());
                    return new RuntimeException("Serviço não encontrado");
                });

        // Check if booking date is not in the past
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            log.warn("Attempt to book in the past: date={}", request.getBookingDate());
            throw new RuntimeException("Não é possível agendar em datas passadas");
        }

        // Check if client already has a booking at this time
        List<Booking> clientConflicts = bookingRepository.findConflictingBookingsForClient(
                userId,
                request.getBookingDate(),
                request.getBookingTime()
        );
        
        if (!clientConflicts.isEmpty()) {
            log.warn("Client already has a booking at date={}, time={}", 
                request.getBookingDate(), request.getBookingTime());
            throw new RuntimeException("Você já possui um agendamento neste horário. Por favor, escolha outro horário.");
        }

        UUID barberUserId = null;
        
        // If barber preference is specified, check availability
        if (request.getBarberId() != null && !request.getBarberId().isEmpty()) {
            log.debug("Barber preference specified: barberId={}", request.getBarberId());
            // barberId from request is the ID from barbers table, need to convert to user_id
            UUID barberTableId = UUID.fromString(request.getBarberId());
            Barber barber = barberRepository.findById(barberTableId)
                    .orElseThrow(() -> {
                        log.error("Barber not found: barberTableId={}", barberTableId);
                        return new RuntimeException("Barbeiro não encontrado");
                    });
            
            if (!barber.getActive()) {
                log.warn("Attempt to book with inactive barber: barberTableId={}", barberTableId);
                throw new RuntimeException("Este barbeiro não está disponível no momento");
            }
            
            // Use the user_id (not the barber table id) for bookings
            barberUserId = barber.getUserId();
            log.debug("Barber userId resolved: {}", barberUserId);
            
            // Check if barber is available at this time
            List<Booking> barberConflicts = bookingRepository.findConflictingBookingsForBarber(
                    barberUserId,
                    request.getBookingDate(),
                    request.getBookingTime()
            );
            
            if (!barberConflicts.isEmpty()) {
                log.warn("Barber not available: barberUserId={}, date={}, time={}, conflicts={}", 
                    barberUserId, request.getBookingDate(), request.getBookingTime(), barberConflicts.size());
                throw new RuntimeException("Este barbeiro não está disponível neste horário. Por favor, escolha outro horário ou outro barbeiro.");
            }
            
            log.info("Barber {} is available for booking", barberUserId);
        } else {
            // Auto-assign barber: find available barber
            log.debug("Auto-assigning barber for date={}, time={}", 
                request.getBookingDate(), request.getBookingTime());
            barberUserId = findAvailableBarber(request.getBookingDate(), request.getBookingTime());
            
            if (barberUserId == null) {
                log.warn("No available barbers found for date={}, time={}", 
                    request.getBookingDate(), request.getBookingTime());
                throw new RuntimeException("Nenhum barbeiro disponível neste horário. Por favor, escolha outro horário.");
            }
            
            log.info("Auto-assigned barber: {}", barberUserId);
        }

        // Create booking
        Booking booking = Booking.builder()
                .userId(userId)
                .service(service)
                .barberId(barberUserId)
                .bookingDate(request.getBookingDate())
                .bookingTime(request.getBookingTime())
                .status(Booking.BookingStatus.PENDING)
                .notes(request.getNotes())
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created successfully: bookingId={}, userId={}, barberUserId={}, date={}, time={}", 
            booking.getId(), userId, barberUserId, request.getBookingDate(), request.getBookingTime());
        
        // Send booking confirmation email
        try {
            String bookingDateFormatted = booking.getBookingDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String bookingTimeFormatted = booking.getBookingTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            emailService.sendBookingConfirmationEmail(
                userId,
                service.getName(),
                bookingDateFormatted,
                bookingTimeFormatted
            );
            log.debug("Booking confirmation email sent to userId={}", userId);
        } catch (Exception e) {
            // Log error but don't fail the booking creation
            log.error("Failed to send booking confirmation email for userId={}", userId, e);
        }
        
        return mapBookingToDTO(booking);
    }
    
    private UUID findAvailableBarber(LocalDate date, java.time.LocalTime time) {
        List<Barber> activeBarbers = barberRepository.findByActiveTrue();
        log.info("Finding available barber for date={}, time={}. Active barbers count: {}", 
            date, time, activeBarbers.size());
        
        if (activeBarbers.isEmpty()) {
            log.warn("No active barbers found in the system");
            return null;
        }
        
        for (Barber barber : activeBarbers) {
            log.debug("Checking barber userId={} for availability", barber.getUserId());
            // Use user_id (not barber table id) for booking conflicts
            List<Booking> conflicts = bookingRepository.findConflictingBookingsForBarber(
                    barber.getUserId(),
                    date,
                    time
            );
            
            log.debug("Barber userId={} has {} conflicts", barber.getUserId(), conflicts.size());
            
            if (conflicts.isEmpty()) {
                log.info("Found available barber: userId={}", barber.getUserId());
                // Return user_id for the booking
                return barber.getUserId();
            }
        }
        
        log.warn("No available barbers found for date={}, time={}", date, time);
        return null;
    }

    public List<BookingDTO> getUserBookings(UUID userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDescBookingTimeDesc(userId)
                .stream()
                .map(this::mapBookingToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getUpcomingBookings(UUID userId) {
        return bookingRepository.findUpcomingBookingsByUser(userId)
                .stream()
                .map(this::mapBookingToDTO)
                .collect(Collectors.toList());
    }
    
    // Barber methods
    public List<BookingDTO> getBarberBookings(UUID barberId) {
        log.info("Getting bookings for barber with userId: {}", barberId);
        List<Booking> bookings = bookingRepository.findByBarberIdOrderByBookingDateDescBookingTimeDesc(barberId);
        log.info("Found {} bookings for barber {}", bookings.size(), barberId);
        return bookings.stream()
                .map(this::mapBookingToDTO)
                .collect(Collectors.toList());
    }
    
    public List<BookingDTO> getUpcomingBookingsByBarber(UUID barberId) {
        return bookingRepository.findUpcomingBookingsByBarber(barberId)
                .stream()
                .map(this::mapBookingToDTO)
                .collect(Collectors.toList());
    }
    
    // Admin methods
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAllOrderByBookingDateDescBookingTimeDesc()
                .stream()
                .map(this::mapBookingToDTO)
                .collect(Collectors.toList());
    }

    public BookingDTO getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        return mapBookingToDTO(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(UUID userId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verify ownership
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para cancelar este agendamento");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Agendamento já está cancelado");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Não é possível cancelar um agendamento concluído");
        }

        // Cancel booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        return mapBookingToDTO(booking);
    }

    @Transactional
    public void deleteBooking(UUID userId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verify ownership
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Você não tem permissão para deletar este agendamento");
        }

        bookingRepository.delete(booking);
    }

    // Barber methods
    @Transactional
    public BookingDTO confirmBookingByBarber(UUID barberId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verify that the booking belongs to this barber
        if (booking.getBarberId() == null || !booking.getBarberId().equals(barberId)) {
            throw new RuntimeException("Este agendamento não pertence a você");
        }

        // Check if booking can be confirmed
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Não é possível confirmar um agendamento cancelado");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Agendamento já foi concluído");
        }

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Agendamento já está confirmado");
        }

        // Confirm booking
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmationEmail(
                    booking.getUserId(),
                    booking.getService().getName(),
                    booking.getBookingDate().toString(),
                    booking.getBookingTime().toString()
            );
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email", e);
        }

        return mapBookingToDTO(booking);
    }

    @Transactional
    public BookingDTO completeBookingByBarber(UUID barberId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verify that the booking belongs to this barber
        if (booking.getBarberId() == null || !booking.getBarberId().equals(barberId)) {
            throw new RuntimeException("Este agendamento não pertence a você");
        }

        // Check if booking can be completed
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Não é possível concluir um agendamento cancelado");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Agendamento já foi concluído");
        }

        // Complete booking
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        return mapBookingToDTO(booking);
    }

    @Transactional
    public BookingDTO cancelBookingByBarber(UUID barberId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Verify that the booking belongs to this barber
        if (booking.getBarberId() == null || !booking.getBarberId().equals(barberId)) {
            throw new RuntimeException("Este agendamento não pertence a você");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Agendamento já está cancelado");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Não é possível cancelar um agendamento concluído");
        }

        // Cancel booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        return mapBookingToDTO(booking);
    }

    // Helper methods
    private ServiceDTO mapServiceToDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId().toString())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .active(service.getActive())
                .build();
    }

    private BookingDTO mapBookingToDTO(Booking booking) {
        // Get barber name
        String barberName = null;
        if (booking.getBarberId() != null) {
            try {
                UserInfo barberInfo = getUserInfo(booking.getBarberId());
                if (barberInfo != null && barberInfo.getFullName() != null && !barberInfo.getFullName().isEmpty()) {
                    barberName = barberInfo.getFullName();
                } else {
                    log.warn("Barber name not found for barberId: {}", booking.getBarberId());
                    // Try to get from barber repository as fallback
                    Optional<Barber> barber = barberRepository.findByUserId(booking.getBarberId());
                    if (barber.isPresent()) {
                        barberName = "Barbeiro " + booking.getBarberId().toString().substring(0, 8);
                    }
                }
            } catch (Exception e) {
                log.error("Error getting barber name for barberId: {}", booking.getBarberId(), e);
            }
        }
        
        // Get client name
        String clientName = null;
        try {
            log.info("Getting client name for booking userId: {}", booking.getUserId());
            UserInfo clientInfo = getUserInfo(booking.getUserId());
            if (clientInfo != null && clientInfo.getFullName() != null && !clientInfo.getFullName().isEmpty()) {
                clientName = clientInfo.getFullName();
                log.info("✅ Client name set to: {} for userId: {}", clientName, booking.getUserId());
            } else {
                log.warn("⚠️ Client name not found for userId: {}. clientInfo={}", booking.getUserId(), clientInfo);
            }
        } catch (Exception e) {
            log.error("❌ Error getting client name for userId: {}", booking.getUserId(), e);
        }
        
        return BookingDTO.builder()
                .id(booking.getId().toString())
                .userId(booking.getUserId().toString())
                .clientName(clientName)
                .barberId(booking.getBarberId() != null ? booking.getBarberId().toString() : null)
                .barberName(barberName)
                .service(mapServiceToDTO(booking.getService()))
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus().name())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    
    private UserInfo getUserInfo(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.info("Fetching user info from: {} for userId: {}", url, userId);
            UserInfo userInfo = restTemplate.getForObject(url, UserInfo.class);
            if (userInfo != null) {
                log.info("✅ Fetched user info for userId {}: fullName={}, email={}", userId, userInfo.getFullName(), userInfo.getEmail());
            } else {
                log.warn("⚠️ UserInfo is null for userId: {}", userId);
            }
            return userInfo;
        } catch (Exception e) {
            log.error("❌ Error fetching user info for userId: {} from URL: {}. Error: {}", userId, userServiceUrl + "/api/users/" + userId, e.getMessage(), e);
            return null;
        }
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserInfo {
        private String id;
        private String email;
        private String fullName;
        private String phone;
        private String role;
        private Boolean emailVerified;
    }
}



