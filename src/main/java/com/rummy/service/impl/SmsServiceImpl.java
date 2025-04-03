package com.rummy.service.impl;

import com.rummy.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsServiceImpl implements SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${otp.default.value}")
    private String defaultOtp;

    @Override
    public void sendOtp(String mobileNumber, String otp) {
        // Since we're using a default OTP, we just log the action
        logger.info("Default OTP {} would be used for mobile number {}", defaultOtp, mobileNumber);
    }
}