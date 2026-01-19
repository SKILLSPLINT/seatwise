package com.seatwise.event_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

/**
 * Schema class for Swagger documentation of multipart form data.
 * This class is only used for OpenAPI documentation, not for actual request binding.
 */
@Schema(description = "Multipart form data for creating an event")
public class CreateEventFormData {

    @Schema(description = "Event details in JSON format", requiredMode = Schema.RequiredMode.REQUIRED)
    private CreateEventRequest event;

    @Schema(description = "Event image file (JPEG, PNG)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string", format = "binary")
    private MultipartFile image;

    public CreateEventRequest getEvent() {
        return event;
    }

    public void setEvent(CreateEventRequest event) {
        this.event = event;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}