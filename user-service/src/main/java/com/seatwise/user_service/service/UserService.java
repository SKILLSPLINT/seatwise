package com.seatwise.user_service.service;

import com.seatwise.user_service.dto.request.AdminRegisterRequest;
import com.seatwise.user_service.dto.request.LoginRequest;
import com.seatwise.user_service.dto.request.RegisterRequest;
import com.seatwise.user_service.dto.response.LoginResponse;
import com.seatwise.user_service.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    UserResponse register(RegisterRequest request, MultipartFile profileImage);
    LoginResponse login(LoginRequest request);
    UserResponse getUserProfile(UUID userId);
    UserResponse registerAdmin(AdminRegisterRequest request, MultipartFile profileImage);
}
