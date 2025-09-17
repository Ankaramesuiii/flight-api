package com.example.vol.dtos;

import java.time.LocalDate;

public record VolDto(
        LocalDate dateDepart,
        LocalDate dateArrivee,
        String villeDepart,
        String villeArrivee,
        Double prix,
        Integer tempsTrajet
) {}