package com.example.vol.dtos;

import java.util.UUID;

public record ReservationResponse(
        UUID reservationId,
        UUID volId,
        String nom,
        String email,
        Integer nombrePlaces,
        Integer placesRestantes
) {}