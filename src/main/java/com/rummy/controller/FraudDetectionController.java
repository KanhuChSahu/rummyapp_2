package com.rummy.controller;

import com.rummy.dto.FraudReportDto;
import com.rummy.model.FraudReport;
import com.rummy.model.FraudReportStatus;
import com.rummy.service.FraudDetectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class FraudDetectionController {
    @Autowired
    private FraudDetectionService fraudDetectionService;

    @PostMapping("/flag-fraud")
    public ResponseEntity<?> reportFraud(@Valid @RequestBody FraudReportDto reportDto) {
        try {
            FraudReport report = fraudDetectionService.reportFraud(reportDto);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/fraud-reports")
    public ResponseEntity<?> getUserFraudReports(@PathVariable Long userId) {
        try {
            List<FraudReport> reports = fraudDetectionService.getUserFraudReports(userId);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/fraud-reports/{reportId}/status")
    public ResponseEntity<?> updateFraudReportStatus(
            @PathVariable Long reportId,
            @RequestParam FraudReportStatus status) {
        try {
            FraudReport updatedReport = fraudDetectionService.updateFraudReportStatus(reportId, status);
            return ResponseEntity.ok(updatedReport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}