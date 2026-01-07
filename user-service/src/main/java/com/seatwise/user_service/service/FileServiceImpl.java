package com.seatwise.user_service.service;

import enums.EFileSizeType;
import enums.EFileStatus;
import exception.ResourceNotFoundException;
import com.seatwise.user_service.model.File;
import com.seatwise.user_service.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * FileService implementation.
 * Currently, logs file operations as a placeholder for future Docker container storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public File saveFile(MultipartFile multipartFile) {
        log.info("FileService.saveFile() called - File: {}, Size: {} bytes, ContentType: {}",
                multipartFile.getOriginalFilename(),
                multipartFile.getSize(),
                multipartFile.getContentType());

        // TODO: Store file in Docker container (file-service) in future
        // For now, create file entity with metadata and log the operation
        File file = new File();
        file.setName(multipartFile.getOriginalFilename());
        file.setPath("placeholder/path/" + UUID.randomUUID()); // Placeholder path
        file.setSize((int) multipartFile.getSize());
        file.setSizeType(EFileSizeType.B);
        file.setType(multipartFile.getContentType());
        file.setStatus(EFileStatus.PENDING); // Will be ACTIVE when stored in a Docker container

        File savedFile = fileRepository.save(file);
        log.info("File metadata saved to database - File ID: {}, Status: PENDING (will be stored in Docker container later)",
                savedFile.getId());

        return savedFile;
    }

    @Override
    public File getFileById(UUID fileId) {
        log.info("FileService.getFileById() called - File ID: {}", fileId);
        // TODO: Retrieve file from Docker container (file-service) in future
        return fileRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.warn("File not found - File ID: {}", fileId);
                    return new ResourceNotFoundException("File", "id", fileId.toString());
                });
    }

    @Override
    public void deleteFile(UUID fileId) {
        log.info("FileService.deleteFile() called - File ID: {}", fileId);
        // TODO: Delete file from Docker container (file-service) in future
        File file = getFileById(fileId);
        fileRepository.delete(file);
        log.info("File deleted from database - File ID: {} (will be deleted from Docker container later)", fileId);
    }
}

