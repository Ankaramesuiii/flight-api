package com.example.vol.events;

import com.example.vol.dtos.ReservationDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationSuccessEvent {
    private final Object source;
    private final ReservationDto dto;
    private final int placesDisponiblesAvant;
}
