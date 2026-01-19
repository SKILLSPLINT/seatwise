package com.seatwise.file_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String filename;
    private String originalFilename;
    private String category;
    private String path;
    private String url;
    private String contentType;
    private long size;
    private String sizeFormatted;
    private Instant uploadedAt;
}
