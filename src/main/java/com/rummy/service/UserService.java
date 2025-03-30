package com.rummy.service;

import com.rummy.dto.UserRegistrationDto;
import com.rummy.dto.UserLoginDto;
import com.rummy.dto.UserProfileDto;
import com.rummy.model.User;
import com.rummy.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

        // TODO: Integrate with SMS service to send OTP
        System.out.println("Login OTP for testing: " + otp);
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
            // TODO: Integrate with SMS service to send OTP
            System.out.println("Verification OTP for new mobile number: " + otp);
        }

        return userRepository.save(user);
    }

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long IP_BLOCK_DURATION_MINUTES = 30;
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private Map<String, LocalDateTime> blockedIPs = new HashMap<>();

    public Map<String, Object> loginWithOTP(UserLoginDto loginDto, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // Check if IP is blocked
        if (isIpBlocked(clientIp)) {
            throw new UserServiceException("Too many failed attempts. Please try again later.");
        }

        try {
            User user = userRepository.findByMobileNumber(loginDto.getMobileNumber())
                .orElseThrow(() -> new UserServiceException("User not found"));

            if (!user.isVerified()) {
                throw new UserServiceException("Mobile number not verified");
            }

            if (user.getOtp() == null || user.getOtpExpiryTime() == null) {
                throw new UserServiceException("Please request OTP first");
            }

            if (LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
                throw new UserServiceException("OTP has expired");
            }

            if (!user.getOtp().equals(loginDto.getOtp())) {
                recordFailedAttempt(clientIp);
                throw new UserServiceException("Invalid OTP");
            }

            // Reset login attempts on successful login
            loginAttempts.remove(clientIp);

            // Update last login IP
            user.setLastLoginIp(clientIp);
            user.setOtp(null);
            user.setOtpExpiryTime(null);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("kycStatus", user.getKycStatus());
            response.put("balance", user.getBalance());
            return response;
        } catch (UserServiceException e) {
            recordFailedAttempt(clientIp);
            throw e;
        }
    }

    private boolean isIpBlocked(String ip) {
        LocalDateTime blockedTime = blockedIPs.get(ip);
        if (blockedTime != null) {
            if (LocalDateTime.now().isBefore(blockedTime.plusMinutes(IP_BLOCK_DURATION_MINUTES))) {
                return true;
            } else {
                // Unblock IP after duration
                blockedIPs.remove(ip);
                loginAttempts.remove(ip);
            }
        }
        return false;
    }

    private void recordFailedAttempt(String ip) {
        int attempts = loginAttempts.getOrDefault(ip, 0) + 1;
        loginAttempts.put(ip, attempts);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockedIPs.put(ip, LocalDateTime.now());
        }
    }
}
