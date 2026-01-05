package com.seatwise.user_service.service;

import com.seatwise.user_service.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * FileService interface for handling file operations.
 * Currently logs operations as placeholder for future Docker container implementation.
 */
public interface FileService {
    /**
     * Save a file (currently logs only, will be stored in Docker container later)
     *
     * @param multipartFile The file to save
     * @return File entity with metadata
     */
    File saveFile(MultipartFile multipartFile);

    /**
     * Get file by ID (currently logs only)
     *
     * @param fileId The file ID
     * @return File entity
     */
    File getFileById(UUID fileId);

    /**
     * Delete file by ID (currently logs only)
     *
     * @param fileId The file ID
     */
    void deleteFile(UUID fileId);
}

