package com.seatwise.user_service.controller;

import com.seatwise.user_service.dto.request.AdminRegisterRequest;
import com.seatwise.user_service.dto.request.LoginRequest;
import com.seatwise.user_service.dto.request.RegisterRequest;
import com.seatwise.user_service.dto.response.ApiResponse;
import com.seatwise.user_service.dto.response.LoginResponse;
import com.seatwise.user_service.dto.response.UserResponse;
import com.seatwise.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

        private final UserService userService;

        @PostMapping("/register")
        @Operation(summary = "Register a new user", description = "Registers a new user account. Profile picture can be uploaded separately using the profile picture upload endpoint.")

        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone number already exists", content = @Content)
        })
        public ResponseEntity<ApiResponse<UserResponse>> register(
                        @Valid @RequestBody RegisterRequest dto,
                        HttpServletRequest request) {
                String userTimeZone = request.getHeader("time-zone");
                UserResponse userResponse = userService.register(dto, userTimeZone);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("User registered successfully", userResponse));
        }

        @PostMapping("/login")
        @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token (placeholder until Spring Security is integrated). "
                        +
                        "Can login with either email or phone number.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
        })
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {
                log.info("POST /api/v1/auth/login - Login attempt");
                String userTimeZone = httpRequest.getHeader("time-zone");
                LoginResponse loginResponse = userService.login(request, userTimeZone);
                return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        }

        @GetMapping("/user/{id}")
        @Operation(summary = "Get user profile", description = "Retrieves user profile information by user ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
        })
        public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
                        @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,
                        HttpServletRequest request) {
                log.info("GET /api/v1/users/{} - Fetching user profile", id);
                String userTimeZone = request.getHeader("time-zone");
                UserResponse userResponse = userService.getUserProfile(id, userTimeZone);
                return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
        }

        @PostMapping("/admin/register")
        @Operation(summary = "Register a new admin", description = "Creates a new admin account with ADMIN role. Requires a valid admin secret key. Profile picture can be uploaded separately using the profile picture upload endpoint.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid admin secret key", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone number already exists", content = @Content)
        })
        public ResponseEntity<ApiResponse<UserResponse>> registerAdmin(
                        @Valid @RequestBody AdminRegisterRequest request,
                        HttpServletRequest httpRequest) {
                String userTimeZone = httpRequest.getHeader("time-zone");
                UserResponse adminResponse = userService.registerAdmin(request, userTimeZone);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Admin registered successfully", adminResponse));
        }

        @PutMapping(value = "user/{userId}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload or update profile picture", description = "Uploads or updates the profile picture for a user. Accepts image files. Works for both regular users and admins.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or file is empty", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
        })
        public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
                        @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
                        @Parameter(name = "profilePicture", description = "Profile picture image file", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("profile") MultipartFile file,
                        HttpServletRequest request) {
                log.info("PUT /api/v1/auth/users/{}/profile-picture - Uploading profile picture", userId);
                String userTimeZone = request.getHeader("time-zone");
                UserResponse userResponse = userService.uploadProfilePicture(userId, file, userTimeZone);
                return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully", userResponse));
        }
}