package com.example.vol.services;

import com.example.vol.entities.Vol;
import com.example.vol.repositories.VolRepository;
import com.example.vol.repositories.VolSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VolService {

    private final VolRepository volRepository;

    public Page<Vol> findAll(
            LocalDate dateDepart,
            LocalDate dateArrivee,
            String villeDepart,
            String villeArrivee,
            Pageable pageable
    ) {
        Specification<Vol> spec = VolSpecification.filter(dateDepart, dateArrivee, villeDepart, villeArrivee);
        return volRepository.findAll(spec, pageable);
    }

    public List<Vol> saveAll(List<Vol> vols) {
        return volRepository.saveAll(vols);
    }

    @Cacheable(cacheNames = "placesDisponibles", key = "#volId")
    public int getPlacesDisponibles(UUID volId) {
        Vol vol = volRepository.findById(volId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        return vol.getPlacesRestantes();
    }
}