package com.example.vol.services;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.entities.Reservation;
import com.example.vol.entities.Vol;
import com.example.vol.events.ReservationFailedEvent;
import com.example.vol.events.ReservationSuccessEvent;
import com.example.vol.repositories.ReservationRepository;
import com.example.vol.repositories.VolRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.sqlite.SQLiteException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VolRepository volRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Retryable(
            value = {OptimisticLockingFailureException.class, SQLiteException.class, DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public Reservation createReservation(ReservationDto dto) {
        UUID volId;
        try {
            volId = UUID.fromString(dto.volId().toString());
        } catch (IllegalArgumentException e) {
            publishFailureAudit("INVALID-UUID", dto.email(), dto.nombrePlaces(), 0,
                    "Invalid UUID format: " + dto.volId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flight ID format");
        }

        Vol vol = volRepository.findById(volId)
                .orElseThrow(() -> {
                    String errorMsg = "Flight not found with ID: " + volId;
                    publishFailureAudit(dto.volId().toString(), dto.email(), dto.nombrePlaces(), 0, errorMsg);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, errorMsg);
                });

        int availableBefore = vol.getPlacesRestantes();

        if (availableBefore < dto.nombrePlaces()) {
            publishFailureAudit(dto.volId().toString(), dto.email(), dto.nombrePlaces(), availableBefore,
                    "Not enough seats. Remaining: " + availableBefore);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough seats available. Remaining: " + availableBefore);
        }

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

        return saved;
    }

    // Recover methods
    /**
     * Recovery method for optimistic lock conflicts.
     * This method is called automatically if all retries fail.
     */
    @Recover
    public Reservation recoverOptimisticLock(OptimisticLockingFailureException e, ReservationDto dto) {
        publishFailureAudit(dto.volId().toString(), dto.email(), dto.nombrePlaces(), 0,
                "Reservation failed due to high concurrency");
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reservation failed due to high concurrency. Please try again.");
    }

    @Recover
    public Reservation recoverSqliteLock(SQLiteException e, ReservationDto dto) {
        publishFailureAudit(dto.volId().toString(), dto.email(), dto.nombrePlaces(), 0,
                "Reservation failed due to SQLite database lock");
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reservation failed due to SQLite database lock. Please try again.");
    }

    @Recover
    public Reservation recoverDataAccess(DataAccessException e, ReservationDto dto) {
        publishFailureAudit(dto.volId().toString(), dto.email(), dto.nombrePlaces(), 0,
                "Reservation failed due to database access error");
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reservation failed due to database access error. Please try again.");
    }

    // Audit publisher

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
}
