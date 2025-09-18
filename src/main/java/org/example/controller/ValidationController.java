package org.example.controller;

import org.example.service.FrameworkValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = "*")
public class ValidationController {

    @Autowired
    private FrameworkValidationService validationService;

    @GetMapping("/framework")
    public ResponseEntity<FrameworkValidationService.ValidationResult> validateFramework() {
        try {
            FrameworkValidationService.ValidationResult result = validationService.validateFrameworkWithRealData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Framework validation service is running");
    }
}
