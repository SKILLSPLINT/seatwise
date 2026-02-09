package dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmailPayload {
    private final String subject;
    private final String body;
    private final String recipient;
    private final String sender;
    private  UUID notificationId;
}
