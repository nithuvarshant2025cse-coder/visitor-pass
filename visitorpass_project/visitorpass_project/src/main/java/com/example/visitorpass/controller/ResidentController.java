package com.example.visitorpass.controller;

import com.example.visitorpass.dto.ResidentRequest;
import com.example.visitorpass.entity.Resident;
import com.example.visitorpass.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {
    private final ResidentService service;

    public ResidentController(ResidentService service) { this.service = service; }

    @GetMapping public List<Resident> all() { return service.findAll(); }

    @GetMapping("/{id}") public Resident one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<Resident> create(@Valid @RequestBody ResidentRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    public Resident update(@PathVariable Long id, @Valid @RequestBody ResidentRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
