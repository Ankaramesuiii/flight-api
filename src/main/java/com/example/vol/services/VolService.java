package com.example.vol.services;

import com.example.vol.entities.Vol;
import com.example.vol.entities.Vol;
import com.example.vol.repositories.VolRepository;
import com.example.vol.repositories.VolSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VolService {

    private final VolRepository volRepository;

    public List<Vol> findAll(
            LocalDateTime dateDepart,
            LocalDateTime dateArrivee,
            String villeDepart,
            String villeArrivee,
            String tri
    ) {
        Specification<Vol> spec = VolSpecification.filter(dateDepart, dateArrivee, villeDepart, villeArrivee);

        Sort sort = Sort.unsorted();
        if ("prix".equalsIgnoreCase(tri)) {
            sort = Sort.by("prix").ascending();
        } else if ("temps_trajet".equalsIgnoreCase(tri)) {
            sort = Sort.by("tempsTrajet").ascending();
        }

        return volRepository.findAll(spec, sort);
    }

    public List<Vol> saveAll(List<Vol> vols) {
        return volRepository.saveAll(vols);
    }
}