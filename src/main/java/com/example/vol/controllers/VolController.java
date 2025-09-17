package com.example.vol.controllers;

import com.example.vol.dtos.VolDto;
import com.example.vol.entities.Vol;
import com.example.vol.services.VolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vols")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VolController {

    private final VolService volService;

    @GetMapping
    public Map<String, Object> getAllVols(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateArrivee,
            @RequestParam(required = false) String villeDepart,
            @RequestParam(required = false) String villeArrivee,
            Pageable pageable
    ) {
        Page<Vol> page = volService.findAll(dateDepart, dateArrivee, villeDepart, villeArrivee, pageable);

        Map<String, Object> response = new HashMap<>();
        System.out.println(page);
        response.put("content", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());

        return response;

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