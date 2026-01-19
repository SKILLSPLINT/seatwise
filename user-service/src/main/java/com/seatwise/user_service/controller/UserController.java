package com.seatwise.user_service.controller;

import com.seatwise.user_service.dto.request.AdminRegisterRequest;
import com.seatwise.user_service.dto.request.LoginRequest;
import com.seatwise.user_service.dto.request.RegisterRequest;
import dto.BaseResponse;
import com.seatwise.user_service.dto.response.LoginResponse;
import com.seatwise.user_service.dto.response.UserResponse;
import com.seatwise.user_service.service.UserService;
import exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or phone number already exists", content = @Content)
    })
    public ResponseEntity<BaseResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest dto,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        UserResponse userResponse = userService.register(dto, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("User registered successfully", userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token (placeholder until Spring Security is integrated). "
            +
            "Can login with either email or phone number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("POST /api/v1/auth/login - Login attempt");
        String userTimeZone = httpRequest.getHeader("time-zone");
        LoginResponse loginResponse = userService.login(request, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Login successful", loginResponse));
    }

    @GetMapping("/user/me")
    @Operation(summary = "Get current user profile",
            description = "Retrieves the authenticated user's profile information from the JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<BaseResponse<UserResponse>> getUserProfile(HttpServletRequest request) {
        String userEmail = request.getHeader("X-User-Email");
        if (userEmail == null || userEmail.isEmpty()) {
            log.warn("GET /api/v1/auth/user/me - Missing X-User-Email header");
            throw new UnauthorizedException("Unauthorized - Missing user information");
        }
        log.info("GET /api/v1/auth/user/me - Fetching user profile for: {}", userEmail);
        String userTimeZone = request.getHeader("time-zone");
        UserResponse userResponse = userService.getUserProfile(userEmail, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("User profile retrieved successfully", userResponse));
    }

    @PostMapping("/admin/register")
    @Operation(summary = "Register a new admin", description = "Creates a new admin account with ADMIN role. Requires a valid admin secret key. Profile picture can be uploaded separately using the profile picture upload endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid admin secret key", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or phone number already exists", content = @Content)
    })
    public ResponseEntity<BaseResponse<UserResponse>> registerAdmin(
            @Valid @RequestBody AdminRegisterRequest request,
            HttpServletRequest httpRequest) {
        String userTimeZone = httpRequest.getHeader("time-zone");
        UserResponse adminResponse = userService.registerAdmin(request, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Admin registered successfully", adminResponse));
    }

    @PutMapping(value = "/user/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or update profile picture",
            description = "Uploads or updates the authenticated user's profile picture. Accepts image files. Works for both regular users and admins.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
            security = @SecurityRequirement(name = "bearerAuth")

    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or file is empty", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<BaseResponse<UserResponse>> uploadProfilePicture(
            @Parameter(name = "profilePicture", description = "Profile picture image file", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("profile") MultipartFile file,
            HttpServletRequest request) {
        String userEmail = request.getHeader("X-User-Email");
        if (userEmail == null || userEmail.isEmpty()) {
            log.warn("PUT /api/v1/auth/user/me/profile-picture - Missing X-User-Email header");
            throw new UnauthorizedException("Unauthorized - Missing user information");
        }
        log.info("PUT /api/v1/auth/user/me/profile-picture - Uploading profile picture for: {}", userEmail);
        String userTimeZone = request.getHeader("time-zone");
        UserResponse userResponse = userService.uploadProfilePicture(userEmail, file, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Profile picture uploaded successfully", userResponse));
    }
}