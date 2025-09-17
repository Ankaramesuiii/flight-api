package com.example.vol.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vols")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vol {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "date_depart", nullable = false)
    private LocalDate dateDepart;

    @Column(name = "date_arrivee", nullable = false)
    private LocalDate dateArrivee;

    @Column(name = "ville_depart", nullable = false, length = 100)
    private String villeDepart;

    @Column(name = "ville_arrivee", nullable = false, length = 100)
    private String villeArrivee;

    @Column(nullable = false)
    private Double prix;

    @Column(name = "temps_trajet", nullable = false)
    private Integer tempsTrajet; // en minutes
}
