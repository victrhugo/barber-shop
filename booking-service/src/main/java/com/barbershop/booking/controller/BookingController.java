package com.barbershop.booking.controller;

import com.barbershop.booking.dto.BookingDTO;
import com.barbershop.booking.dto.CreateBookingRequest;
import com.barbershop.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request) {
        try {
            BookingDTO booking = bookingService.createBooking(UUID.fromString(userId), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings(
            @RequestHeader("X-User-Id") String userId) {
        List<BookingDTO> bookings = bookingService.getUserBookings(UUID.fromString(userId));
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingDTO>> getUpcomingBookings(
            @RequestHeader("X-User-Id") String userId) {
        List<BookingDTO> bookings = bookingService.getUpcomingBookings(UUID.fromString(userId));
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String bookingId) {
        try {
            BookingDTO booking = bookingService.getBookingById(UUID.fromString(bookingId));
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String bookingId) {
        try {
            BookingDTO booking = bookingService.cancelBooking(
                    UUID.fromString(userId),
                    UUID.fromString(bookingId)
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String bookingId) {
        try {
            bookingService.deleteBooking(UUID.fromString(userId), UUID.fromString(bookingId));
            return ResponseEntity.ok(Map.of("message", "Agendamento deletado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Barber endpoints
    @GetMapping("/barber/my-bookings")
    public ResponseEntity<List<BookingDTO>> getBarberBookings(
            @RequestHeader("X-User-Id") String barberId) {
        List<BookingDTO> bookings = bookingService.getBarberBookings(UUID.fromString(barberId));
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/barber/upcoming")
    public ResponseEntity<List<BookingDTO>> getBarberUpcomingBookings(
            @RequestHeader("X-User-Id") String barberId) {
        List<BookingDTO> bookings = bookingService.getUpcomingBookingsByBarber(UUID.fromString(barberId));
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/barber/{bookingId}/confirm")
    public ResponseEntity<?> confirmBookingByBarber(
            @RequestHeader("X-User-Id") String barberId,
            @PathVariable String bookingId) {
        try {
            BookingDTO booking = bookingService.confirmBookingByBarber(
                    UUID.fromString(barberId),
                    UUID.fromString(bookingId)
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/barber/{bookingId}/complete")
    public ResponseEntity<?> completeBookingByBarber(
            @RequestHeader("X-User-Id") String barberId,
            @PathVariable String bookingId) {
        try {
            BookingDTO booking = bookingService.completeBookingByBarber(
                    UUID.fromString(barberId),
                    UUID.fromString(bookingId)
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/barber/{bookingId}/cancel")
    public ResponseEntity<?> cancelBookingByBarber(
            @RequestHeader("X-User-Id") String barberId,
            @PathVariable String bookingId) {
        try {
            BookingDTO booking = bookingService.cancelBookingByBarber(
                    UUID.fromString(barberId),
                    UUID.fromString(bookingId)
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Admin endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestHeader("X-User-Id") String adminId) {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
}



