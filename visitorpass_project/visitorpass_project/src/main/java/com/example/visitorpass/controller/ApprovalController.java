package com.example.visitorpass.controller;

import com.example.visitorpass.dto.ApprovalRequest;
import com.example.visitorpass.dto.ApprovalResponse;
import com.example.visitorpass.service.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService service;

    public ApprovalController(ApprovalService service) { this.service = service; }

    @GetMapping
    public List<ApprovalResponse> all(@RequestParam(required = false) Long residentId) { return service.findAll(residentId); }

    @GetMapping("/{id}") public ApprovalResponse one(@PathVariable Long id) { return service.findOne(id); }

    @PostMapping
    public ResponseEntity<ApprovalResponse> create(@Valid @RequestBody ApprovalRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    public ApprovalResponse update(@PathVariable Long id, @Valid @RequestBody ApprovalRequest r) { return service.update(id, r); }

    @PutMapping("/{id}/revoke") public ApprovalResponse revoke(@PathVariable Long id) { return service.revoke(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
