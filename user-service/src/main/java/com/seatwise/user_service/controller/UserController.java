package com.seatwise.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/users/register", 
                 consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with USER role. Email and phone number must be unique. " +
                    "Profile image is optional. " +
                    "Supports both JSON body (application/json) and multipart/form-data. " +
                    "For multipart: send form fields (email, password, phoneNumber, firstName, lastName, gender) and optional profileImage file."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email or phone number already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Parameter(description = "User registration data")
            @RequestPart(value = "request", required = false) String jsonRequest,
            @ModelAttribute @Valid RegisterRequest requestModel,
            @RequestBody(required = false) @Valid RegisterRequest requestBody,
            @Parameter(description = "Profile image file (optional, only for multipart)")
            @RequestParam(required = false) MultipartFile profileImage) {
        log.info("POST /api/v1/users/register - Registering new user");
        
        RegisterRequest request = null;
        MultipartFile file = null;
        
        // Determine content type and parse accordingly
        if (jsonRequest != null && !jsonRequest.trim().isEmpty()) {
            // JSON in request part (multipart with JSON)
            try {
                request = objectMapper.readValue(jsonRequest, RegisterRequest.class);
                file = profileImage;
                log.info("Parsed JSON from request part");
            } catch (Exception e) {
                log.error("Error parsing JSON request part: {}", e.getMessage());
                throw new com.seatwise.common.exception.BadRequestException("Invalid JSON format in request part: " + e.getMessage());
            }
        } else if (requestBody != null) {
            // JSON body (application/json)
            request = requestBody;
            log.info("Using JSON request body");
        } else if (requestModel != null) {
            // Form data (multipart/form-data)
            request = requestModel;
            file = profileImage;
            log.info("Using form data model");
        } else {
            throw new com.seatwise.common.exception.BadRequestException("Request data is required");
        }
        
        if (request == null) {
            throw new com.seatwise.common.exception.BadRequestException("Request data is required");
        }
        
        UserResponse userResponse = userService.register(request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    @PostMapping("/auth/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token (placeholder until Spring Security is integrated). " +
                    "Can login with either email or phone number."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Login attempt");
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @GetMapping("/users/{id}")
    @Operation(
            summary = "Get user profile",
            description = "Retrieves user profile information by user ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        log.info("GET /api/v1/users/{} - Fetching user profile", id);
        UserResponse userResponse = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
    }

    @PostMapping(value = "/admin/register",
                 consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(
            summary = "Register a new admin",
            description = "Creates a new admin account with ADMIN role. Requires a valid admin secret key. " +
                    "Profile image is optional. " +
                    "Supports both JSON body (application/json) and multipart/form-data."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Admin registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid admin secret key",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email or phone number already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> registerAdmin(
            @Parameter(description = "Admin registration data")
            @RequestPart(value = "request", required = false) String jsonRequest,
            @ModelAttribute @Valid AdminRegisterRequest requestModel,
            @RequestBody(required = false) @Valid AdminRegisterRequest requestBody,
            @Parameter(description = "Profile image file (optional, only for multipart)")
            @RequestParam(required = false) MultipartFile profileImage) {
        log.info("POST /api/v1/admin/register - Registering new admin");
        
        AdminRegisterRequest request = null;
        MultipartFile file = null;
        
        // Determine content type and parse accordingly
        if (jsonRequest != null && !jsonRequest.trim().isEmpty()) {
            // JSON in request part (multipart with JSON)
            try {
                request = objectMapper.readValue(jsonRequest, AdminRegisterRequest.class);
                file = profileImage;
                log.info("Parsed JSON from request part");
            } catch (Exception e) {
                log.error("Error parsing JSON request part: {}", e.getMessage());
                throw new com.seatwise.common.exception.BadRequestException("Invalid JSON format in request part: " + e.getMessage());
            }
        } else if (requestBody != null) {
            // JSON body (application/json)
            request = requestBody;
            log.info("Using JSON request body");
        } else if (requestModel != null) {
            // Form data (multipart/form-data)
            request = requestModel;
            file = profileImage;
            log.info("Using form data model");
        } else {
            throw new com.seatwise.common.exception.BadRequestException("Request data is required");
        }
        
        if (request == null) {
            throw new com.seatwise.common.exception.BadRequestException("Request data is required");
        }
        
        UserResponse adminResponse = userService.registerAdmin(request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin registered successfully", adminResponse));
    }
}
