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
import org.springframework.transaction.annotation.Transactional;

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
        // Validate service exists
        Service service = serviceRepository.findById(UUID.fromString(request.getServiceId()))
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        // Check if booking date is not in the past
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Não é possível agendar em datas passadas");
        }

        UUID barberUserId = null;
        
        // If barber preference is specified, check availability
        if (request.getBarberId() != null && !request.getBarberId().isEmpty()) {
            // barberId from request is the ID from barbers table, need to convert to user_id
            UUID barberTableId = UUID.fromString(request.getBarberId());
            Barber barber = barberRepository.findById(barberTableId)
                    .orElseThrow(() -> new RuntimeException("Barbeiro não encontrado"));
            
            if (!barber.getActive()) {
                throw new RuntimeException("Barbeiro não está disponível");
            }
            
            // Use the user_id (not the barber table id) for bookings
            barberUserId = barber.getUserId();
            
            // Check if barber is available at this time
            List<Booking> barberConflicts = bookingRepository.findConflictingBookingsForBarber(
                    barberUserId,
                    request.getBookingDate(),
                    request.getBookingTime()
            );
            
            if (!barberConflicts.isEmpty()) {
                throw new RuntimeException("Barbeiro não está disponível neste horário");
            }
        } else {
            // Auto-assign barber: find available barber
            barberUserId = findAvailableBarber(request.getBookingDate(), request.getBookingTime());
            
            if (barberUserId == null) {
                throw new RuntimeException("Nenhum barbeiro disponível neste horário");
            }
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
        } catch (Exception e) {
            // Log error but don't fail the booking creation
            log.error("Failed to send booking confirmation email", e);
        }
        
        return mapBookingToDTO(booking);
    }
    
    private UUID findAvailableBarber(LocalDate date, java.time.LocalTime time) {
        List<Barber> activeBarbers = barberRepository.findByActiveTrue();
        
        for (Barber barber : activeBarbers) {
            // Use user_id (not barber table id) for booking conflicts
            List<Booking> conflicts = bookingRepository.findConflictingBookingsForBarber(
                    barber.getUserId(),
                    date,
                    time
            );
            
            if (conflicts.isEmpty()) {
                // Return user_id for the booking
                return barber.getUserId();
            }
        }
        
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
        return bookingRepository.findByBarberIdOrderByBookingDateDescBookingTimeDesc(barberId)
                .stream()
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
        String barberName = null;
        if (booking.getBarberId() != null) {
            Optional<Barber> barber = barberRepository.findByUserId(booking.getBarberId());
            if (barber.isPresent()) {
                // barber.getBarberId() is the user_id, which is what we store in bookings.barber_id
                // For now we'll use a placeholder, but ideally we'd fetch from user-service
                barberName = "Barbeiro " + booking.getBarberId().toString().substring(0, 8);
            }
        }
        
        return BookingDTO.builder()
                .id(booking.getId().toString())
                .userId(booking.getUserId().toString())
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
}



