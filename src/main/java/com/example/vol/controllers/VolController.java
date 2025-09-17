package com.example.vol.controllers;

import com.example.vol.dtos.VolDto;
import com.example.vol.entities.Vol;
import com.example.vol.services.VolService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vols")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VolController {

    private final VolService volService;

    @GetMapping
    public List<Vol> getAllVols(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDepart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateArrivee,
            @RequestParam(required = false) String villeDepart,
            @RequestParam(required = false) String villeArrivee,
            @RequestParam(required = false) String tri
    ) {
        return volService.findAll(dateDepart, dateArrivee, villeDepart, villeArrivee, tri);
    }

    @PostMapping
    public List<Vol> createVols(@RequestBody List<VolDto> volsDto) {
        List<Vol> vols = volsDto.stream().map(dto -> Vol.builder()
                .dateDepart(dto.dateDepart())
                .dateArrivee(dto.dateArrivee())
                .villeDepart(dto.villeDepart())
                .villeArrivee(dto.villeArrivee())
                .prix(dto.prix())
                .tempsTrajet(dto.tempsTrajet())
                .build()).toList();

        return volService.saveAll(vols);
    }
}