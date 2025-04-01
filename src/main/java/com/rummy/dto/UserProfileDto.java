package com.rummy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileDto {
    @NotBlank(message = "Username is required")
    private String username;
    
    private String avatar;
    
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number format")
    private String mobileNumber;
}