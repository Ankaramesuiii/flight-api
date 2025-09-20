package com.example.vol.controllers;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.dtos.ReservationResponse;
import com.example.vol.entities.Reservation;
import com.example.vol.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ReservationResponse createReservation(@RequestBody @Valid ReservationDto dto) {
        Reservation reservation = reservationService.createReservation(dto);

        return new ReservationResponse(
                reservation.getId(),
                reservation.getVol().getId(),
                reservation.getNom(),
                reservation.getEmail(),
                reservation.getNombrePlaces(),
                reservation.getVol().getPlacesRestantes()
        );
    }
}
