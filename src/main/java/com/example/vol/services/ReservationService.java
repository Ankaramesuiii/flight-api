package com.example.vol.services;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.entities.Reservation;
import com.example.vol.entities.Vol;
import com.example.vol.events.ReservationFailedEvent;
import com.example.vol.events.ReservationSuccessEvent;
import com.example.vol.exceptions.PlacesInsuffisantesException;
import com.example.vol.exceptions.VolNotFoundException;
import com.example.vol.repositories.ReservationRepository;
import com.example.vol.repositories.VolRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VolRepository volRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a reservation.
     * Retries only on DB/concurrency issues, NOT on domain exceptions.
     */
    @Transactional
    public Reservation createReservation(ReservationDto dto) {
        if (dto.volId() == null) {
            publishFailureAudit("NULL", dto.email(), dto.nombrePlaces(), 0,
                    "Flight ID is null");
            throw new IllegalArgumentException("Flight ID cannot be null");
        }

        UUID volId = dto.volId();

        Vol vol = volRepository.findById(volId)
                .orElseThrow(() -> {
                    publishFailureAudit(volId.toString(), dto.email(), dto.nombrePlaces(), 0,
                            "Flight not found with ID: " + volId);
                    return new VolNotFoundException(volId.toString());
                });

        int availableBefore = vol.getPlacesRestantes();
        if (availableBefore < dto.nombrePlaces()) {
            publishFailureAudit(volId.toString(), dto.email(), dto.nombrePlaces(), availableBefore,
                    "Not enough seats. Remaining: " + availableBefore);
            // Domain exception is thrown directly
            throw new PlacesInsuffisantesException(dto.nombrePlaces(), availableBefore);
        }

        // DB operation wrapped in a private retryable method
        return saveReservationWithRetry(dto, vol, availableBefore);
    }

    // Separate retryable method only for DB/concurrency
    @Retryable(
            value = {OptimisticLockingFailureException.class, DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    private Reservation saveReservationWithRetry(ReservationDto dto, Vol vol, int availableBefore) {
        vol.setPlacesRestantes(availableBefore - dto.nombrePlaces());
        volRepository.save(vol);

        Reservation reservation = Reservation.builder()
                .nom(dto.nom())
                .email(dto.email())
                .nombrePlaces(dto.nombrePlaces())
                .vol(vol)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.publishEvent(new ReservationSuccessEvent(this, dto, availableBefore));
        evictCache(vol.getId());
        return saved;
    }

    // ---------------------
    // Retry recovery methods
    // ---------------------
    @Recover
    public Reservation recoverDatabaseException(DataAccessException e, ReservationDto dto) {
        publishFailureAudit(dto.volId() != null ? dto.volId().toString() : "NULL",
                dto.email(), dto.nombrePlaces(), 0,
                "Reservation failed due to database error: " + e.getMessage());
        throw new RuntimeException("Reservation failed due to database error. Please try again.", e);
    }

    @Recover
    public Reservation recoverOptimisticLock(OptimisticLockingFailureException e, ReservationDto dto) {
        publishFailureAudit(dto.volId() != null ? dto.volId().toString() : "NULL",
                dto.email(), dto.nombrePlaces(), 0,
                "Reservation failed due to high concurrency: " + e.getMessage());
        throw new RuntimeException("Reservation failed due to high concurrency. Please try again.", e);
    }

    // ---------------------
    // Audit helper
    // ---------------------
    private void publishFailureAudit(String volId, String email, int placesDemandees, int placesAvant, String msgErreur) {
        try {
            ReservationFailedEvent event = new ReservationFailedEvent(
                    this,
                    volId,
                    email,
                    placesDemandees,
                    placesAvant,
                    msgErreur
            );
            eventPublisher.publishEvent(event);
            log.warn("Audit event published: [volId={}, email={}, msg={}]", volId, email, msgErreur);
        } catch (Exception e) {
            log.error("Failed to publish audit event", e);
        }
    }

    // ---------------------
    // Cache eviction
    // ---------------------
    @CacheEvict(cacheNames = "placesDisponibles", key = "#volId")
    public void evictCache(UUID volId) {
    }
}
