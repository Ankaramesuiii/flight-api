package com.example.vol.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime timestamp;

    private UUID volId;

    private String emailPassager;

    private Integer placesDemandees;

    private Integer placesDisponiblesAvant;

    @Enumerated(EnumType.STRING)
    private Status statut;

    private String messageErreur;

    public enum Status {
        SUCCESS, FAILED
    }
}
