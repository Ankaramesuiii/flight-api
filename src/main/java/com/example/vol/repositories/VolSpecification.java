package com.example.vol.repositories;

import com.example.vol.entities.Vol;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class VolSpecification {

    public static Specification<Vol> filter(
            LocalDate dateDepart,
            LocalDate dateArrivee,
            String villeDepart,
            String villeArrivee
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (dateDepart != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("dateDepart"), dateDepart));
            }
            if (dateArrivee != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("dateArrivee"), dateArrivee));
            }
            if (villeDepart != null && !villeDepart.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("villeDepart"), villeDepart.toLowerCase()));
            }
            if (villeArrivee != null && !villeArrivee.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("villeArrivee"), villeArrivee.toLowerCase()));
            }

            return predicate;
        };
    }
}
