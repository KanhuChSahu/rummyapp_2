package com.rummy.controller;

import com.rummy.dto.KycDocumentDto;
import com.rummy.model.KycStatus;
import com.rummy.service.KycService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
public class KycController {
    @Autowired
    private KycService kycService;

    @PostMapping("/upload-aadhaar")
    public ResponseEntity<?> uploadAadhaar(@Valid @ModelAttribute KycDocumentDto documentDto) {
        try {
            return ResponseEntity.ok(kycService.uploadDocument(documentDto, "AADHAAR"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload-pan")
    public ResponseEntity<?> uploadPan(@Valid @ModelAttribute KycDocumentDto documentDto) {
        try {
            return ResponseEntity.ok(kycService.uploadDocument(documentDto, "PAN"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyKyc(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(kycService.verifyUserDocuments(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<?> getKycStatus(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(kycService.getUserKycStatus(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateKycStatus(
            @PathVariable Long userId,
            @RequestParam KycStatus status) {
        try {
            return ResponseEntity.ok(kycService.updateUserKycStatus(userId, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}