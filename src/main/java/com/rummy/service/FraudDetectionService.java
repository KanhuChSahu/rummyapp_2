package com.rummy.service;

import com.rummy.dto.FraudReportDto;
import com.rummy.model.FraudReport;
import com.rummy.model.FraudReportStatus;
import com.rummy.model.User;
import com.rummy.repository.FraudReportRepository;
import com.rummy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudDetectionService {
    @Autowired
    private FraudReportRepository fraudReportRepository;

    @Autowired
    private UserRepository userRepository;

    public FraudReport reportFraud(FraudReportDto reportDto) {
        User user = userRepository.findById(reportDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FraudReport report = new FraudReport();
        report.setUser(user);
        report.setReason(reportDto.getReason());
        report.setEvidence(reportDto.getEvidence());
        report.setAdditionalDetails(reportDto.getAdditionalDetails());

        return fraudReportRepository.save(report);
    }

    public List<FraudReport> getUserFraudReports(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return fraudReportRepository.findByUser(user);
    }

    public FraudReport updateFraudReportStatus(Long reportId, FraudReportStatus status) {
        FraudReport report = fraudReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Fraud report not found"));
        
        report.setStatus(status);
        return fraudReportRepository.save(report);
    }
}