package constants;

public final class RabbitConstants {
    public  RabbitConstants(){}
// exchange
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String DLX = "notification.dlx";

    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_DLX = "email.dlx";
//    Queues
    public  static final String EMAIL_QUEUE = "email.queue";
    public  static final String EMAIL_RETRY_QUEUE = "email.retry.queue";
    public static final String EMAIL_DLQ = "email.dlq";

    public  static final String NOTIFICATION_QUEUE = "notification.queue";
    public  static final String DLQ = "notification.dlq";

// routing keys
    public  static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public  static final String DLQ_ROUTING_KEY = "notification.failure";


    public  static final String EMAIL_ROUTING_KEY = "email.send";
    public static final String EMAIL_RETRY_ROUTING_KEY = "email.retry";
    public static final String EMAIL_DLQ_ROUTING_KEY = "email.failure";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String BOOKING_CONFIRMATION_QUEUE = "booking.confirmation.queue";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
}

