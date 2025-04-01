package com.rummy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KycDocumentDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Document type is required")
    private String documentType; // AADHAAR or PAN
    
    @NotBlank(message = "Document number is required")
    private String documentNumber;
    
    @NotNull(message = "Document file is required")
    private MultipartFile documentFile;
}