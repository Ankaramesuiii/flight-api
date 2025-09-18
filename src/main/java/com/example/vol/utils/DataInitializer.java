package com.example.vol.utils;

import com.example.vol.repositories.VolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VolRepository volRepository;

    @Override
    public void run(String... args) {
        volRepository.findAll().forEach(vol -> {
            if (vol.getCapacite() == null) vol.setCapacite(180);
            if (vol.getPlacesRestantes() == null) vol.setPlacesRestantes(180);
            if (vol.getVersion() == null) vol.setVersion(0L);
            volRepository.save(vol);
        });
    }
}
