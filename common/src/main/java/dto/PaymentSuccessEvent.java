package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID bookingId;
    private UUID userId;
    private String userEmail;
    private String transactionRef;
    private Double amount;
}
