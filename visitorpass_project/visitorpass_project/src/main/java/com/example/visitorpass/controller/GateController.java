package com.example.visitorpass.controller;

import com.example.visitorpass.dto.ApprovalResponse;
import com.example.visitorpass.dto.OtpValidationRequest;
import com.example.visitorpass.service.GateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gate")
public class GateController {
    private final GateService service;

    public GateController(GateService service) { this.service = service; }

    @PostMapping("/validate")
    public ApprovalResponse validate(@Valid @RequestBody OtpValidationRequest r) { return service.validate(r.otp()); }

    @GetMapping("/entries") public List<ApprovalResponse> entries() { return service.entryLog(); }
}
