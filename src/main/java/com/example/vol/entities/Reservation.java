package com.example.vol.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    private String nom;
    private String email;
    private Integer nombrePlaces;

    @ManyToOne
    @JoinColumn(name = "vol_id")
    private Vol vol;
}
