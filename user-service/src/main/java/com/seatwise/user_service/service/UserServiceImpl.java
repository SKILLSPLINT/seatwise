package com.seatwise.user_service.service;

import com.seatwise.user_service.Utils.JwtUtils;
import enums.ERole;
import exception.BadRequestException;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.UnauthorizedException;
import utils.TimeUtils;
import com.seatwise.user_service.dto.request.AdminRegisterRequest;
import com.seatwise.user_service.dto.request.LoginRequest;
import com.seatwise.user_service.dto.request.RegisterRequest;
import com.seatwise.user_service.dto.response.LoginResponse;
import com.seatwise.user_service.dto.response.UserResponse;
import com.seatwise.user_service.model.File;
import com.seatwise.user_service.model.Role;
import com.seatwise.user_service.model.User;
import com.seatwise.user_service.repository.RoleRepository;
import com.seatwise.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Value("${admin.secret-key}")
    private String adminSecretKey;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request, String userTimeZone) {
        // Validate user doesn't already exist (email or phone)
        validateUserDoesNotExist(request.getEmail(), request.getPhoneNumber());

        // Create a new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setGender(request.getGender());

        // Assign a USER role (guaranteed to exist after RoleInitializer)
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> {
                    log.error("USER role not found in database - this should not happen!");
                    return new RuntimeException("USER role not found. Please check database initialization.");
                });
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser, userTimeZone);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, String userTimeZone) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Find the user by email or phone number
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .orElseGet(() -> userRepository.findByPhoneNumber(request.getEmailOrPhone())
                        .orElse(null));

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - Invalid password for user: {}", user.getEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("Login successful - User ID: {}, Email: {}", user.getId(), user.getEmail());
        Role lastAdded = null;
        for (Role r : user.getRoles()) {
            lastAdded = r;
        }
        assert lastAdded != null;
        String token = JwtUtils.generateToken(lastAdded.getName(), user.getEmail());
        return LoginResponse.builder()
                .token(token)
                .user(mapToUserResponse(user, userTimeZone))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String userEmail, String userTimeZone) {
        log.info("Fetching user profile - User Email: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("User not found - User Email: {}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        return mapToUserResponse(user, userTimeZone);
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(AdminRegisterRequest request, String userTimeZone) {
        log.info("Registering new admin with email: {}", request.getEmail());

        // Validate an admin secret key
        if (!adminSecretKey.equals(request.getAdminSecretKey())) {
            log.warn("Admin registration failed - Invalid secret key");
            throw new UnauthorizedException("Invalid admin secret key");
        }

        // Validate user doesn't already exist (email or phone)
        validateUserDoesNotExist(request.getEmail(), request.getPhoneNumber());

        // Create a new admin user
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setFirstname(request.getFirstName());
        admin.setLastname(request.getLastName());
        admin.setGender(request.getGender());

        // Assign ADMIN role (guaranteed to exist after RoleInitializer)
        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseThrow(() -> {
                    log.error("ADMIN role not found in database - this should not happen!");
                    return new RuntimeException("ADMIN role not found. Please check database initialization.");
                });
        admin.getRoles().add(adminRole);

        User savedAdmin = userRepository.save(admin);
        log.info("Admin registered successfully - Admin ID: {}, Email: {}", savedAdmin.getId(), savedAdmin.getEmail());

        return mapToUserResponse(savedAdmin, userTimeZone);
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePicture(String userEmail, MultipartFile profilePicture, String userTimeZone) {
        log.info("Uploading profile picture for user Email: {}", userEmail);

        // Validate a file
        if (profilePicture == null || profilePicture.isEmpty()) {
            log.warn("Profile picture upload failed - File is empty for user Email: {}", userEmail);
            throw new BadRequestException("Profile picture file cannot be empty");
        }

        // Validate file is an image
        String contentType = profilePicture.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Profile picture upload failed - Invalid file type: {} for user Email: {}", contentType, userEmail);
            throw new BadRequestException("File must be an image");
        }

        // Find user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Profile picture upload failed - User not found: {}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        // Delete old profile picture if exists
        if (user.getProfileImage() != null) {
            log.info("Deleting old profile picture - File ID: {} for user Email: {}", user.getProfileImage().getId(), userEmail);
            try {
                fileService.deleteFile(user.getProfileImage().getId());
            } catch (Exception e) {
                log.warn("Failed to delete old profile picture: {}", e.getMessage());
                // Continue with upload even if deletion fails
            }
        }

        // Save a new profile picture
        try {
            File file = fileService.saveFile(profilePicture);
            user.setProfileImage(file);
            log.info("Profile picture saved - File ID: {} for user Email: {}", file.getId(), userEmail);
        } catch (Exception e) {
            log.error("Error saving profile picture for user Email {}: {}", userEmail, e.getMessage());
            throw new BadRequestException("Failed to save profile picture: " + e.getMessage());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile picture uploaded successfully for user Email: {}", userEmail);

        return mapToUserResponse(updatedUser, userTimeZone);
    }

    /**
     * Validates that a user with the given email or phone number does not exist.
     * Uses a single database query to check both fields efficiently.
     *
     * @param email       the email to check
     * @param phoneNumber the phone number to check
     * @throws ConflictException if a user exists with the given email or phone number
     */
    private void validateUserDoesNotExist(String email, String phoneNumber) {
        if (userRepository.existsByEmailOrPhoneNumber(email, phoneNumber)) {
            // Determine which field already exists for a better error message
            if (userRepository.existsByEmail(email)) {
                log.warn("Registration failed - Email already exists: {}", email);
                throw new ConflictException("Email already exists");
            } else {
                log.warn("Registration failed - Phone number already exists: {}", phoneNumber);
                throw new ConflictException("Phone number already exists");
            }
        }
    }

    private UserResponse mapToUserResponse(User user, String userTimeZone) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        String profileImageUrl = null;
        if (user.getProfileImage() != null) {
            // TODO: Build actual URL when file-service Docker container is implemented
            profileImageUrl = "/api/v1/files/" + user.getProfileImage().getId();
        }

        // Convert UTC timestamps to user's timezone
        String createdAt = TimeUtils.toUserOrUtc(user.getCreatedAt(), userTimeZone);
        String updatedAt = TimeUtils.toUserOrUtc(user.getUpdatedAt(), userTimeZone);

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .gender(user.getGender())
                .roles(roleNames)
                .profileImageUrl(profileImageUrl)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
