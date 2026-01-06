package com.seatwise.user_service.service;

import com.seatwise.user_service.dto.request.AdminRegisterRequest;
import com.seatwise.user_service.dto.request.LoginRequest;
import com.seatwise.user_service.dto.request.RegisterRequest;
import com.seatwise.user_service.dto.response.LoginResponse;
import com.seatwise.user_service.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    UserResponse register(RegisterRequest request, String userTimeZone);
    LoginResponse login(LoginRequest request, String userTimeZone);
    UserResponse getUserProfile(UUID userId, String userTimeZone);
    UserResponse registerAdmin(AdminRegisterRequest request, String userTimeZone);
    UserResponse uploadProfilePicture(UUID userId, MultipartFile profilePicture, String userTimeZone);
}
