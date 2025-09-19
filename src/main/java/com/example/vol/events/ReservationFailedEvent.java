package com.example.vol.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationFailedEvent {
    private final Object source;
    private final String volId;
    private final String email;
    private final int placesDemandees;
    private final int placesDisponiblesAvant;
    private final String messageErreur;
}
