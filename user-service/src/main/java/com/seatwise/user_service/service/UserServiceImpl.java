package com.seatwise.user_service.service;

import com.seatwise.common.enums.ERole;
import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ResourceNotFoundException;
import com.seatwise.common.exception.UnauthorizedException;
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
import java.util.UUID;
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
    public UserResponse register(RegisterRequest request, MultipartFile profileImage) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - Email already exists: {}", request.getEmail());
            throw new ConflictException("Email already exists");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed - Phone number already exists: {}", request.getPhoneNumber());
            throw new ConflictException("Phone number already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setGender(request.getGender());

        // Handle profile image if provided (optional)
        if (profileImage != null && !profileImage.isEmpty()) {
            log.info("Processing profile image for user registration");
            try {
                File file = fileService.saveFile(profileImage);
                user.setProfileImage(file);
                log.info("Profile image saved - File ID: {}", file.getId());
            } catch (Exception e) {
                log.error("Error saving profile image during registration: {}", e.getMessage());
                // Continue registration even if file save fails
            }
        } else {
            log.info("No profile image provided for user registration");
        }

        // Assign USER role
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseGet(() -> {
                    log.info("USER role not found, creating new role");
                    Role newRole = new Role();
                    newRole.setName(ERole.USER);
                    newRole.setDescription("Regular user role");
                    return roleRepository.save(newRole);
                });
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully - User ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Find the user by email or phone number
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .orElseGet(() -> userRepository.findByPhoneNumber(request.getEmailOrPhone())
                        .orElse(null));

        if (user == null) {
            log.warn("Login failed - User not found: {}", request.getEmailOrPhone());
            throw new UnauthorizedException("Invalid credentials");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - Invalid password for user: {}", user.getEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("Login successful - User ID: {}, Email: {}", user.getId(), user.getEmail());

        // TODO: Generate JWT token when Spring Security is integrated
        // For now, return placeholder token
        String placeholderToken = "placeholder-jwt-token-" + user.getId();

        return LoginResponse.builder()
                .token(placeholderToken)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        log.info("Fetching user profile - User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found - User ID: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId.toString());
                });

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse registerAdmin(AdminRegisterRequest request, MultipartFile profileImage) {
        log.info("Registering new admin with email: {}", request.getEmail());

        // Validate admin secret key
        if (!adminSecretKey.equals(request.getAdminSecretKey())) {
            log.warn("Admin registration failed - Invalid secret key");
            throw new UnauthorizedException("Invalid admin secret key");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Admin registration failed - Email already exists: {}", request.getEmail());
            throw new ConflictException("Email already exists");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Admin registration failed - Phone number already exists: {}", request.getPhoneNumber());
            throw new ConflictException("Phone number already exists");
        }

        // Create new admin user
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setFirstname(request.getFirstName());
        admin.setLastname(request.getLastName());
        admin.setGender(request.getGender());

        // Handle profile image if provided (optional)
        if (profileImage != null && !profileImage.isEmpty()) {
            log.info("Processing profile image for admin registration");
            try {
                com.seatwise.user_service.model.File file = fileService.saveFile(profileImage);
                admin.setProfileImage(file);
                log.info("Profile image saved - File ID: {}", file.getId());
            } catch (Exception e) {
                log.error("Error saving profile image during admin registration: {}", e.getMessage());
                // Continue registration even if file save fails
            }
        } else {
            log.info("No profile image provided for admin registration");
        }

        // Assign ADMIN role
        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseGet(() -> {
                    log.info("ADMIN role not found, creating new role");
                    Role newRole = new Role();
                    newRole.setName(ERole.ADMIN);
                    newRole.setDescription("Administrator role");
                    return roleRepository.save(newRole);
                });
        admin.getRoles().add(adminRole);

        User savedAdmin = userRepository.save(admin);
        log.info("Admin registered successfully - Admin ID: {}, Email: {}", savedAdmin.getId(), savedAdmin.getEmail());

        return mapToUserResponse(savedAdmin);
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        String profileImageUrl = null;
        if (user.getProfileImage() != null) {
            // TODO: Build actual URL when file-service Docker container is implemented
            profileImageUrl = "/api/v1/files/" + user.getProfileImage().getId();
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .gender(user.getGender())
                .roles(roleNames)
                .profileImageUrl(profileImageUrl)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
