package com.seatwise.notification_service.controller;

import dto.NotificationRequest;
import dto.NotificationResponse;
import com.seatwise.notification_service.service.NotificationService;
import dto.BaseResponse;
import enums.ENotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
    })
    public ResponseEntity<BaseResponse<NotificationResponse>> create(
            @Valid @RequestBody NotificationRequest dto,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        NotificationResponse notificationResponse = notificationService.createNotification(dto, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Notification created successfully", notificationResponse));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications for a user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
    })
    public ResponseEntity<BaseResponse<Page<NotificationResponse>>> getAllNotifications(
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> notifications = notificationService.getAllNotifications(userId, pageable, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification retrieved successfully", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
    })
    public ResponseEntity<BaseResponse<NotificationResponse>> getNotificationById(
            @PathVariable UUID id,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        NotificationResponse notification = notificationService.getNotificationById(id, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Notification retrieved successfully", notification));
    }

    @GetMapping("/user/{userId}/read")
    @Operation(summary = "Get read notifications", description = "Retrieve all read notifications for a user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Read notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
    })
    public ResponseEntity<BaseResponse<Page<NotificationResponse>>> getReadNotifications(
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> notifications = notificationService.getReadNotifications(userId, pageable, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Read notifications retrieved successfully", notifications));
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications for a user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
    })
    public ResponseEntity<BaseResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, pageable, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Unread notifications retrieved successfully", notifications));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
    })
    public ResponseEntity<BaseResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        NotificationResponse notification = notificationService.markAsRead(id, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Notification marked as read", notification));
    }

    @GetMapping("/user/{userId}/search")
    @Operation(summary = "Search notifications", description = "Search notifications by keyword, type, and read status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
    })
    public ResponseEntity<BaseResponse<Page<NotificationResponse>>> searchNotifications(
            @PathVariable UUID userId,
            @Parameter(description = "Search keyword (searches in subject and body)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Notification type filter") @RequestParam(required = false) ENotificationType type,
            @Parameter(description = "Read status filter") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        String userTimeZone = request.getHeader("time-zone");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> notifications = notificationService.searchNotifications(userId, keyword, type, isRead, pageable, userTimeZone);
        return ResponseEntity.ok(BaseResponse.success("Search completed successfully", notifications));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
