package com.example.vol.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationDto(
        @NotNull(message = "volId is required")
        UUID volId,

        @NotBlank(message = "Nom is required")
        String nom,

        @Email(message = "Email should be valid")
        String email,

        @Min(value = 1, message = "Nombre de places must be at least 1")
        int nombrePlaces
) {}
