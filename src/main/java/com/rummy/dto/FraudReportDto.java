package com.rummy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FraudReportDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Reason is required")
    private String reason;
    
    private String evidence;
    private String additionalDetails;
}