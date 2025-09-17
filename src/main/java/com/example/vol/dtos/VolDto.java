package com.example.vol.dtos;

import java.time.LocalDateTime;

public record VolDto(
        LocalDateTime dateDepart,
        LocalDateTime dateArrivee,
        String villeDepart,
        String villeArrivee,
        Double prix,
        Integer tempsTrajet
) {}