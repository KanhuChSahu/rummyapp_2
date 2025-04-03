package com.rummy.controller;

import com.rummy.dto.UserRegistrationDto;
import com.rummy.exception.UserServiceException;
import com.rummy.dto.UserLoginDto;
import com.rummy.dto.UserProfileDto;
import com.rummy.model.User;
import com.rummy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            HttpServletRequest request) {
        try {
            User user = userService.registerUser(registrationDto, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please verify your mobile number.");
            response.put("userId", user.getId());
            response.put("mobileNumber", user.getMobileNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam("otp") String otp) {
        try {
            boolean verified = userService.verifyOTP(mobileNumber, otp);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mobile number verified successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (UserServiceException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login/request-otp")
    public ResponseEntity<?> requestLoginOTP(@RequestParam("mobileNumber") String mobileNumber) {
        try {
            userService.requestLoginOTP(mobileNumber);
            return ResponseEntity.ok().body("OTP sent successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        try {
            Map<String, Object> response = userService.loginWithCredentials(loginDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | UserServiceException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserProfile(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileDto profileDto) {
        try {
            User updatedUser = userService.updateUserProfile(userId, profileDto);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}