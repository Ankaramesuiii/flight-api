package com.example.vol.repositories;

import com.example.vol.entities.AuditReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditReservationRepository extends JpaRepository<AuditReservation, Long> {
}
