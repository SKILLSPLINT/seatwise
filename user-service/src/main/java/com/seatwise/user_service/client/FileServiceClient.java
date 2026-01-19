package com.seatwise.user_service.client;

import com.seatwise.user_service.dto.response.FileUploadResponseDto;
import com.seatwise.user_service.dto.response.PresignedUrlResponse;
import dto.BaseResponse;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class FileServiceClient {

    private final WebClient webClient;

    public FileServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.file-service.url:http://FILE-SERVICE}") String fileServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(fileServiceUrl).build();
    }

    public FileUploadResponseDto uploadFile(MultipartFile file, String category) {
        try {
            log.info("Uploading file to file-service: {} category: {}", file.getOriginalFilename(), category);

            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", file.getResource())
                    .filename(filename);
            bodyBuilder.part("category", category);

            BaseResponse<FileUploadResponseDto> response = webClient.post()
                    .uri("/api/v1/files/upload")
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<FileUploadResponseDto>>() {
                    })
                    .block();

            if (response != null && response.isSuccess()) {
                log.info("File uploaded successfully: {}", response.getData().getFilename());
                return response.getData();
            }

            throw new RuntimeException("Failed to upload file: " +
                    (response != null ? response.getMessage() : "Unknown error"));

        } catch (WebClientResponseException e) {
            log.error("Error calling file-service: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("File service error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public void deleteFile(String category, String filename) {
        try {
            log.info("Deleting file from file-service: {} category: {}", filename, category);

            webClient.delete()
                    .uri("/api/v1/files/{category}/{filename}", category, filename)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("File deleted successfully: {}", filename);

        } catch (WebClientResponseException.NotFound e) {
            log.error("File not found in file-service: {}", filename);
            throw new ResourceNotFoundException("File", "filename", filename);
        } catch (WebClientResponseException e) {
            log.error("Error deleting file from file-service: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public boolean fileExists(String category, String filename) {
        try {
            BaseResponse<Boolean> response = webClient.get()
                    .uri("/api/v1/files/exists/{category}/{filename}", category, filename)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<Boolean>>() {
                    })
                    .block();

            return response != null && Boolean.TRUE.equals(response.getData());

        } catch (WebClientResponseException e) {
            log.error("Error checking file existence: {} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    public String getPresignedUrl(String category, String filename) {
        try {
            log.info("Fetching presigned URL from file-service for {}/{}", category, filename);
            BaseResponse<PresignedUrlResponse> response = webClient.get().uri("/api/v1/files/presigned/{category}/{fileName}", category, filename).retrieve().bodyToMono(new ParameterizedTypeReference<BaseResponse<PresignedUrlResponse>>() {
            }).block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getPresignedUrl();
            }
            throw new RuntimeException("Failed to get presigned URL: " +
                    (response != null ? response.getMessage() : "Unknown error"));
        } catch (
                WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("File", "filename", filename);
        } catch (
                WebClientResponseException e) {
            log.error("File-service error: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("File service error", e);
        }
    }
}
