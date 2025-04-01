package com.rummy.service.impl;

import com.rummy.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsServiceImpl implements SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;
    
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendOtp(String mobileNumber, String otp) {
        try {
            String messageBody = "Your OTP is: " + otp;
            Message message = Message.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                messageBody
            ).create();
            
            logger.info("SMS sent successfully to {}, Message SID: {}", mobileNumber, message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", mobileNumber, e.getMessage());
            throw new RuntimeException("Failed to send OTP via SMS", e);
        }
    }
}