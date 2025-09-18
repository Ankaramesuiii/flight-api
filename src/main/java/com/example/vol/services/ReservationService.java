package com.example.vol.services;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.entities.Reservation;
import com.example.vol.entities.Vol;
import com.example.vol.repositories.ReservationRepository;
import com.example.vol.repositories.VolRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VolRepository volRepository;

    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public Reservation createReservation(ReservationDto dto) {
        Vol vol = volRepository.findById(dto.volId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));

        if (vol.getPlacesRestantes() < dto.nombrePlaces()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough seats available. Remaining: " + vol.getPlacesRestantes());
        }

        vol.setPlacesRestantes(vol.getPlacesRestantes() - dto.nombrePlaces());
        volRepository.save(vol);

        Reservation reservation = Reservation.builder()
                .nom(dto.nom())
                .email(dto.email())
                .nombrePlaces(dto.nombrePlaces())
                .vol(vol)
                .build();

        return reservationRepository.save(reservation);
    }

    /**
     * Recovery method for optimistic lock conflicts.
     * This method is called automatically if all retries fail.
     */
    @Recover
    public Reservation recoverOptimisticLock(OptimisticLockException e, ReservationDto dto) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reservation failed due to high concurrency. Please try again.");
    }
}
