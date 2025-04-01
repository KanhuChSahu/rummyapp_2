package com.rummy.service;

import com.rummy.dto.UserRegistrationDto;
import com.rummy.dto.UserLoginDto;
import com.rummy.dto.UserProfileDto;
import com.rummy.model.User;
import com.rummy.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import com.rummy.exception.UserServiceException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmsService smsService;

    public User registerUser(UserRegistrationDto registrationDto, HttpServletRequest request) {
        // Validate if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new UserServiceException("Passwords do not match");
        }

        // Check if username or mobile number already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserServiceException("Username already exists");
        }
        if (userRepository.existsByMobileNumber(registrationDto.getMobileNumber())) {
            throw new UserServiceException("Mobile number already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setMobileNumber(registrationDto.getMobileNumber());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setLastLoginIp(getClientIp(request));
        
        // Generate and set OTP
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));

        // Send OTP via SMS
        smsService.sendOtp(user.getMobileNumber(), otp);

        return userRepository.save(user);
    }

    public boolean verifyOTP(String mobileNumber, String otp) {
        User user = userRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new UserServiceException("User not found"));

        if (user.getOtp() == null || user.getOtpExpiryTime() == null) {
            throw new UserServiceException("No OTP request found");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            throw new UserServiceException("OTP has expired");
        }

        if (!user.getOtp().equals(otp)) {
            throw new UserServiceException("Invalid OTP");
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
        return true;
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public void requestLoginOTP(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new UserServiceException("User not found or not registered"));

        if (!user.isVerified()) {
            throw new UserServiceException("Mobile number not verified");
        }

        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send OTP via SMS
        smsService.sendOtp(mobileNumber, otp);
    }

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserServiceException("User not found"));
    }

    public User updateUserProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserServiceException("User not found"));

        // Validate if new username is already taken by another user
        if (!user.getUsername().equals(profileDto.getUsername()) &&
            userRepository.existsByUsername(profileDto.getUsername())) {
            throw new UserServiceException("Username already exists");
        }

        // Validate if new mobile number is already taken by another user
        if (profileDto.getMobileNumber() != null &&
            !user.getMobileNumber().equals(profileDto.getMobileNumber()) &&
            userRepository.existsByMobileNumber(profileDto.getMobileNumber())) {
            throw new UserServiceException("Mobile number already registered");
        }

        user.setUsername(profileDto.getUsername());
        if (profileDto.getAvatar() != null) {
            user.setAvatar(profileDto.getAvatar());
        }
        
        // Only update mobile number if it's changed and valid
        if (profileDto.getMobileNumber() != null && 
            !user.getMobileNumber().equals(profileDto.getMobileNumber())) {
            user.setMobileNumber(profileDto.getMobileNumber());
            user.setVerified(false); // Require re-verification for new mobile number
            // Generate and set OTP for new mobile number verification
            String otp = generateOTP();
            user.setOtp(otp);
            user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
            // Send OTP via SMS for new mobile number verification
            smsService.sendOtp(profileDto.getMobileNumber(), otp);
        }

        return userRepository.save(user);
    }

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long IP_BLOCK_DURATION_MINUTES = 30;
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private Map<String, LocalDateTime> blockedIPs = new HashMap<>();

    @Autowired
    private AuthenticationManager authenticationManager;

    public boolean isValidOTP(String mobileNumber, String otp) {
        User user = userRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new UserServiceException("User not found"));

        if (user.getOtp() == null || user.getOtpExpiryTime() == null) {
            throw new UserServiceException("No OTP request found");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            throw new UserServiceException("OTP has expired");
        }

        if (!user.getOtp().equals(otp)) {
            throw new UserServiceException("Invalid OTP");
        }

        return true;
    }

    public Map<String, Object> loginWithCredentials(UserLoginDto loginDto, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // Check if IP is blocked
        if (isIpBlocked(clientIp)) {
            throw new UserServiceException("Too many failed attempts. Please try again later.");
        }

        try {
            // Authenticate using Spring Security
            org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );

            User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new UserServiceException("User not found"));

            if (!user.isVerified()) {
                throw new UserServiceException("Account not verified");
            }

            // Reset login attempts on successful login
            loginAttempts.remove(clientIp);

            // Update last login IP
            user.setLastLoginIp(clientIp);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("kycStatus", user.getKycStatus());
            response.put("balance", user.getBalance());
            return response;
        } catch (AuthenticationException e) {
            recordFailedAttempt(clientIp);
            throw new UserServiceException("Invalid username or password");
        }
    }

    private void recordFailedAttempt(String clientIp) {
        int attempts = loginAttempts.getOrDefault(clientIp, 0) + 1;
        loginAttempts.put(clientIp, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockedIPs.put(clientIp, LocalDateTime.now());
            loginAttempts.remove(clientIp);
            throw new UserServiceException("Account locked due to too many failed attempts");
        }
    }

    private boolean isIpBlocked(String clientIp) {
        LocalDateTime blockedTime = blockedIPs.get(clientIp);
        if (blockedTime != null) {
            if (LocalDateTime.now().isBefore(blockedTime.plusMinutes(IP_BLOCK_DURATION_MINUTES))) {
                return true;
            }
            blockedIPs.remove(clientIp);
        }
        return false;
    }
}
