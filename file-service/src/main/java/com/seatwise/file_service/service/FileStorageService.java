package com.seatwise.file_service.service;

import com.seatwise.file_service.dto.response.FileListResponse;
import com.seatwise.file_service.dto.response.FileUploadResponse;
import com.seatwise.file_service.dto.response.PresignedUrlResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadFile(MultipartFile file, String category);

    InputStreamResource downloadFile(String category, String filename);

    String getContentType(String category, String filename);

    void deleteFile(String category, String filename);

    FileListResponse listFiles(String category);

    PresignedUrlResponse getPresignedUrl(String category, String filename);

    boolean fileExists(String category, String filename);
}
