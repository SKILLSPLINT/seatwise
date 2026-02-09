package dto;

import enums.ENotificationStatus;
import enums.ENotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private ENotificationType type;
    private String subject;
    private String body;
    private String recipient;
    private String sender;
    private ENotificationStatus status;
    private Boolean isRead;
    private String createdAt;
    private String updatedAt;
    private String deliveredAt;
}
