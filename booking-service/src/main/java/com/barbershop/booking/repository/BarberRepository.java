package com.barbershop.booking.repository;

import com.barbershop.booking.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberRepository extends JpaRepository<Barber, UUID> {
    
    Optional<Barber> findByUserId(UUID userId);
    
    List<Barber> findByActiveTrue();
}


