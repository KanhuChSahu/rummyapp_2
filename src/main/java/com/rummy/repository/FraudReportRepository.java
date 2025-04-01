package com.rummy.repository;

import com.rummy.model.FraudReport;
import com.rummy.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudReportRepository extends JpaRepository<FraudReport, Long> {

    List<FraudReport> findByUser(User user);
    // Add custom query methods if needed
}