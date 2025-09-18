package com.example.vol.dtos;

import java.util.UUID;

public record ReservationDto(
        UUID volId,
        String nom,
        String email,
        Integer nombrePlaces
) {}
