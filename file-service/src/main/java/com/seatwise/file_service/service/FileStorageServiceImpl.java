package com.seatwise.file_service.service;

import com.seatwise.file_service.config.FileProperties;
import com.seatwise.file_service.config.MinioProperties;
import com.seatwise.file_service.dto.response.FileListResponse;
import com.seatwise.file_service.dto.response.FileUploadResponse;
import com.seatwise.file_service.dto.response.PresignedUrlResponse;
import com.seatwise.file_service.exception.FileStorageException;
import com.seatwise.file_service.exception.InvalidFileException;
import exception.BadRequestException;
import exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileProperties fileProperties;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String category) {
        validateFile(file);
        validateCategory(category);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFilename = generateUniqueFilename(originalFilename);
        String objectPath = buildObjectPath(category, uniqueFilename);

        try {
            log.info("Uploading file: {} to category: {}", originalFilename, category);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Successfully uploaded file: {} to path: {}", originalFilename, objectPath);

            return FileUploadResponse.builder()
                    .filename(uniqueFilename)
                    .originalFilename(originalFilename)
                    .category(category)
                    .path(objectPath)
                    .url(buildFileUrl(category, uniqueFilename))
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .sizeFormatted(formatFileSize(file.getSize()))
                    .uploadedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file: {} - Error: {}", originalFilename, e.getMessage());
            throw new FileStorageException("Failed to upload file: " + originalFilename, e);
        }
    }

    @Override
    public InputStreamResource downloadFile(String category, String filename) {
        validateCategory(category);
        String objectPath = buildObjectPath(category, filename);

        try {
            log.info("Downloading file: {} from category: {}", filename, category);

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .build()
            );

            return new InputStreamResource(stream);

        } catch (Exception e) {
            log.error("Failed to download file: {} - Error: {}", filename, e.getMessage());
            throw new ResourceNotFoundException("File not found: " + filename);
        }
    }

    @Override
    public String getContentType(String category, String filename) {
        String objectPath = buildObjectPath(category, filename);

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            log.error("Failed to get content type for file: {} - Error: {}", filename, e.getMessage());
            return "application/octet-stream";
        }
    }

    @Override
    public void deleteFile(String category, String filename) {
        validateCategory(category);
        String objectPath = buildObjectPath(category, filename);

        try {
            log.info("Deleting file: {} from category: {}", filename, category);

            if (!fileExists(category, filename)) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .build()
            );

            log.info("Successfully deleted file: {}", objectPath);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete file: {} - Error: {}", filename, e.getMessage());
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public FileListResponse listFiles(String category) {
        validateCategory(category);
        String prefix = category + "/";

        try {
            log.info("Listing files in category: {}", category);

            List<FileListResponse.FileInfo> files = new ArrayList<>();

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .prefix(prefix)
                            .recursive(false)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String filename = item.objectName().replace(prefix, "");

                if (!filename.isEmpty()) {
                    files.add(FileListResponse.FileInfo.builder()
                            .filename(filename)
                            .path(item.objectName())
                            .size(item.size())
                            .sizeFormatted(formatFileSize(item.size()))
                            .lastModified(item.lastModified() != null ?
                                    item.lastModified().toString() : null)
                            .build());
                }
            }

            return FileListResponse.builder()
                    .category(category)
                    .totalFiles(files.size())
                    .files(files)
                    .build();

        } catch (Exception e) {
            log.error("Failed to list files in category: {} - Error: {}", category, e.getMessage());
            throw new FileStorageException("Failed to list files in category: " + category, e);
        }
    }

    @Override
    public PresignedUrlResponse getPresignedUrl(String category, String filename) {
        validateCategory(category);
        String objectPath = buildObjectPath(category, filename);

        try {
            log.info("Generating presigned URL for file: {} in category: {}", filename, category);

            if (!fileExists(category, filename)) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            int expirySeconds = minioProperties.getPresignedUrlExpiry();

            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );

            return PresignedUrlResponse.builder()
                    .filename(filename)
                    .category(category)
                    .presignedUrl(presignedUrl)
                    .expirySeconds(expirySeconds)
                    .expiresAt(Instant.now().plusSeconds(expirySeconds))
                    .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for file: {} - Error: {}", filename, e.getMessage());
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public boolean fileExists(String category, String filename) {
        String objectPath = buildObjectPath(category, filename);

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectPath)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or not provided");
        }

        String contentType = file.getContentType();
        if (contentType == null || !fileProperties.getAllowedTypesList().contains(contentType)) {
            throw new InvalidFileException("File type not allowed: " + contentType +
                    ". Allowed types: " + fileProperties.getAllowedTypes());
        }

        long maxSizeBytes = (long) fileProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException("File size exceeds maximum allowed size of " +
                    fileProperties.getMaxSizeMb() + "MB");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new BadRequestException("Category is required");
        }

        if (!fileProperties.getCategoriesList().contains(category.toLowerCase())) {
            throw new BadRequestException("Invalid category: " + category +
                    ". Allowed categories: " + fileProperties.getCategories());
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String buildObjectPath(String category, String filename) {
        return category.toLowerCase() + "/" + filename;
    }

    private String buildFileUrl(String category, String filename) {
        return "/api/v1/files/" + category + "/" + filename;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
