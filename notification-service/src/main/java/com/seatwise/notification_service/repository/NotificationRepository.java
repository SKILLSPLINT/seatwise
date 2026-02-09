package com.seatwise.notification_service.repository;

import com.seatwise.notification_service.model.Notification;
import enums.ENotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsRead(UUID userId, boolean isRead, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND " +
            "(LOWER(n.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> searchByKeyword(@Param("userId") UUID userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND " +
            "(:type IS NULL OR n.type = :type) AND " +
            "(:isRead IS NULL OR n.isRead = :isRead) AND " +
            "(LOWER(n.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> searchNotifications(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("type") ENotificationType type,
            @Param("isRead") Boolean isRead,
            Pageable pageable);
}
