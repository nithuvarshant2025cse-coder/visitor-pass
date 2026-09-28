package com.example.visitorpass.controller;

import com.example.visitorpass.entity.Flat;
import com.example.visitorpass.service.FlatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flats")
public class FlatController {
    private final FlatService service;

    public FlatController(FlatService service) { this.service = service; }

    @GetMapping public List<Flat> all() { return service.findAll(); }

    @GetMapping("/{id}") public Flat one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<Flat> create(@Valid @RequestBody Flat flat) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(flat));
    }

    @PutMapping("/{id}")
    public Flat update(@PathVariable Long id, @Valid @RequestBody Flat flat) { return service.update(id, flat); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
