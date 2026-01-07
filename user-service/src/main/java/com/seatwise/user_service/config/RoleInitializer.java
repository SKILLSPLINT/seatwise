package com.seatwise.user_service.config;

import enums.ERole;
import com.seatwise.user_service.model.Role;
import com.seatwise.user_service.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes default roles (USER and ADMIN) in the database.
 * Runs once per JVM instance using a static flag to ensure efficiency.
 * Only creates roles if they don't already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;
    private static boolean initialized = false;

    @PostConstruct
    @Transactional
    public void initializeRoles() {
        // Check static flag to ensure we only run once per JVM instance
        if (initialized) {
            log.debug("Role initialization already completed, skipping...");
            return;
        }

        log.info("Initializing default roles...");

        // Initialize USER role
        initializeRole(ERole.USER, "Regular user role");

        // Initialize ADMIN role
        initializeRole(ERole.ADMIN, "Administrator role");

        initialized = true;
        log.info("Role initialization completed successfully");
    }

    private void initializeRole(ERole roleName, String description) {
        roleRepository.findByName(roleName).ifPresentOrElse(
                role -> log.debug("Role '{}' already exists, skipping creation", roleName),
                () -> {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDescription(description);
                    roleRepository.save(role);
                    log.info("Created role: {} - {}", roleName, description);
                }
        );
    }
}

