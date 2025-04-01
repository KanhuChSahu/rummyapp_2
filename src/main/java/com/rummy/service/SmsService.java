package com.rummy.service;

public interface SmsService {
    void sendOtp(String mobileNumber, String otp);
}