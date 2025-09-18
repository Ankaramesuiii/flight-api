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

    @Builder.Default
    private Integer capacite = 180;

    @Builder.Default
    private Integer placesRestantes = 180;

    /**
     * Optimistic locking to prevent overbooking.
     * When multiple users attempt to reserve seats on the same flight simultaneously,
     * Hibernate uses the @Version field in the Vol entity to detect conflicts.
     * If two transactions modify the same flight at the same time,
     * one of them will fail with an OptimisticLockException.
     * The @Retryable annotation automatically retries the reservation in case
     * of such conflicts, up to 3 attempts with a short delay.
     */
    @Version
    @Builder.Default
    private Long version = 0L;
}
