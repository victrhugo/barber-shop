package com.barbershop.booking.repository;

import com.barbershop.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    List<Booking> findByUserIdOrderByBookingDateDescBookingTimeDesc(UUID userId);
    
    List<Booking> findByBookingDateOrderByBookingTime(LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate = :date AND b.bookingTime = :time AND b.status != 'CANCELLED'")
    List<Booking> findConflictingBookings(LocalDate date, LocalTime time);
    
    @Query("SELECT b FROM Booking b WHERE b.barberId = :barberId AND b.bookingDate = :date AND b.bookingTime = :time AND b.status != 'CANCELLED'")
    List<Booking> findConflictingBookingsForBarber(UUID barberId, LocalDate date, LocalTime time);
    
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status IN ('PENDING', 'CONFIRMED') ORDER BY b.bookingDate, b.bookingTime")
    List<Booking> findUpcomingBookingsByUser(UUID userId);
    
    @Query("SELECT b FROM Booking b WHERE b.barberId = :barberId ORDER BY b.bookingDate DESC, b.bookingTime DESC")
    List<Booking> findByBarberIdOrderByBookingDateDescBookingTimeDesc(UUID barberId);
    
    @Query("SELECT b FROM Booking b WHERE b.barberId = :barberId AND b.status IN ('PENDING', 'CONFIRMED') ORDER BY b.bookingDate, b.bookingTime")
    List<Booking> findUpcomingBookingsByBarber(UUID barberId);
    
    @Query("SELECT b FROM Booking b ORDER BY b.bookingDate DESC, b.bookingTime DESC")
    List<Booking> findAllOrderByBookingDateDescBookingTimeDesc();
}



