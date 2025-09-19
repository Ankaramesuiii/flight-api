package com.example.vol.listeners;

import com.example.vol.entities.AuditReservation;
import com.example.vol.events.ReservationFailedEvent;
import com.example.vol.events.ReservationSuccessEvent;
import com.example.vol.repositories.AuditReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationAuditListener {

    private final AuditReservationRepository auditRepository;

    /**
     * Handles the successful reservation event.
     * Triggered ONLY after the main transaction has successfully committed.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReservationSuccessEvent(ReservationSuccessEvent event) {
        try {
            AuditReservation audit = AuditReservation.builder()
                    .timestamp(LocalDateTime.now())
                    .volId(UUID.fromString(event.getDto().volId().toString()))
                    .emailPassager(event.getDto().email())
                    .placesDemandees(event.getDto().nombrePlaces())
                    .placesDisponiblesAvant(event.getPlacesDisponiblesAvant())
                    .statut(AuditReservation.Status.SUCCESS)
                    .messageErreur(null)
                    .build();

            auditRepository.save(audit);
            log.info("Audit saved for SUCCESS reservation: volId={}, email={}",
                    event.getDto().volId(), event.getDto().email());
        } catch (Exception e) {
            log.error("Failed to save audit for success event", e);
        }
    }

    /**
     * Handles the failed reservation event.
     * Runs in a new transaction to ensure audit is written
     * even if the main reservation transaction fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.context.event.EventListener
    public void handleReservationFailedEvent(ReservationFailedEvent event) {
        try {
            UUID volId = null;
            try {
                volId = UUID.fromString(event.getVolId());
            } catch (IllegalArgumentException ex) {
                // UUID invalid
                log.warn("Invalid UUID format received in failure event: {}", event.getVolId());
            }

            AuditReservation audit = AuditReservation.builder()
                    .timestamp(LocalDateTime.now())
                    .volId(volId)
                    .emailPassager(event.getEmail())
                    .placesDemandees(event.getPlacesDemandees())
                    .placesDisponiblesAvant(event.getPlacesDisponiblesAvant())
                    .statut(AuditReservation.Status.FAILED)
                    .messageErreur(event.getMessageErreur())
                    .build();

            auditRepository.save(audit);
            log.info("Audit saved for FAILED reservation: volId={}, email={}, reason={}",
                    event.getVolId(), event.getEmail(), event.getMessageErreur());
        } catch (Exception e) {
            log.error("Failed to save audit for failure event", e);
        }
    }
}
