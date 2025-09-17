package com.example.vol.repositories;

import com.example.vol.entities.Vol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface VolRepository extends JpaRepository<Vol, UUID>, JpaSpecificationExecutor<Vol> {
}
