package com.seatwise.user_service.service;

import com.seatwise.user_service.client.FileServiceClient;
import com.seatwise.user_service.dto.response.FileUploadResponseDto;
import enums.EFileSizeType;
import enums.EFileStatus;
import exception.ResourceNotFoundException;
import com.seatwise.user_service.model.File;
import com.seatwise.user_service.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FileServiceClient fileServiceClient;

    @Override
    @Transactional
    public File saveFile(MultipartFile multipartFile) {
        return saveFile(multipartFile, "profile");
    }

    @Transactional
    public File saveFile(MultipartFile multipartFile, String category) {
        log.info("FileService.saveFile() called - File: {}, Size: {} bytes, ContentType: {}, Category: {}",
                multipartFile.getOriginalFilename(),
                multipartFile.getSize(),
                multipartFile.getContentType(),
                category);

        try {
            FileUploadResponseDto uploadResponse = fileServiceClient.uploadFile(multipartFile, category);

            File file = new File();
            file.setName(uploadResponse.getFilename());
            file.setPath(uploadResponse.getPath());
            file.setUrl(uploadResponse.getUrl());
            file.setSize((int) uploadResponse.getSize());
            file.setSizeType(determineSizeType(uploadResponse.getSize()));
            file.setType(uploadResponse.getContentType());
            file.setStatus(EFileStatus.SAVED);

            File savedFile = fileRepository.save(file);
            log.info("File uploaded and metadata saved - File ID: {}, Path: {}",
                    savedFile.getId(), savedFile.getPath());

            return savedFile;

        } catch (Exception e) {
            log.error("Failed to upload file: {} - Error: {}", multipartFile.getOriginalFilename(), e.getMessage());

            File file = new File();
            file.setName(multipartFile.getOriginalFilename());
            file.setPath("failed/" + UUID.randomUUID());
            file.setSize((int) multipartFile.getSize());
            file.setSizeType(determineSizeType(multipartFile.getSize()));
            file.setType(multipartFile.getContentType());
            file.setStatus(EFileStatus.FAILED);

            File savedFile = fileRepository.save(file);
            log.warn("File metadata saved with FAILED status - File ID: {}", savedFile.getId());

            throw new RuntimeException("Failed to upload file to storage service", e);
        }
    }

    @Override
    public File getFileById(UUID fileId) {
        log.info("FileService.getFileById() called - File ID: {}", fileId);
        return fileRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.warn("File not found - File ID: {}", fileId);
                    return new ResourceNotFoundException("File", "id", fileId.toString());
                });
    }

    @Override
    @Transactional
    public void deleteFile(UUID fileId) {
        log.info("FileService.deleteFile() called - File ID: {}", fileId);

        File file = getFileById(fileId);

        try {
            String category = extractCategoryFromPath(file.getPath());
            String filename = extractFilenameFromPath(file.getPath());

            fileServiceClient.deleteFile(category, filename);

            file.setStatus(EFileStatus.DELETED);
            fileRepository.save(file);

            log.info("File deleted from storage and marked as DELETED - File ID: {}", fileId);

        } catch (Exception e) {
            log.error("Failed to delete file from storage: {} - Error: {}", fileId, e.getMessage());
            throw new RuntimeException("Failed to delete file from storage service", e);
        }
    }

    @Override
    public String presSignedUrl(String category, String filename) {
        return fileServiceClient.getPresignedUrl(category, filename);
    }

    private EFileSizeType determineSizeType(long bytes) {
        if (bytes < 1024) {
            return EFileSizeType.B;
        } else if (bytes < 1024 * 1024) {
            return EFileSizeType.KB;
        } else if (bytes < 1024L * 1024 * 1024) {
            return EFileSizeType.MB;
        } else {
            return EFileSizeType.GB;
        }
    }

    private String extractCategoryFromPath(String path) {
        if (path != null && path.contains("/")) {
            return path.substring(0, path.indexOf("/"));
        }
        return "profile";
    }

    private String extractFilenameFromPath(String path) {
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }
}
