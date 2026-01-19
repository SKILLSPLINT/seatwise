package com.seatwise.file_service.controller;

import com.seatwise.file_service.dto.response.FileListResponse;
import com.seatwise.file_service.dto.response.FileUploadResponse;
import com.seatwise.file_service.dto.response.PresignedUrlResponse;
import com.seatwise.file_service.service.FileStorageService;
import dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for file upload, download, and management")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a file",
            description = "Upload a file to the specified category. Returns file metadata including the generated filename and URL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or category"),
            @ApiResponse(responseCode = "413", description = "File too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BaseResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Category for the file (e.g., profile, documents, events)", required = true)
            @RequestParam("category") String category) {

        log.info("Received upload request for file: {} to category: {}",
                file.getOriginalFilename(), category);

        FileUploadResponse response = fileStorageService.uploadFile(file, category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("File uploaded successfully", response));
    }

    @GetMapping("/{category}/{filename}")
    @Operation(
            summary = "Download a file",
            description = "Download a file by category and filename"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "File category", required = true)
            @PathVariable String category,
            @Parameter(description = "Filename to download", required = true)
            @PathVariable String filename) {

        log.info("Received download request for file: {} in category: {}", filename, category);

        InputStreamResource resource = fileStorageService.downloadFile(category, filename);
        String contentType = fileStorageService.getContentType(category, filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/{category}/{filename}")
    @Operation(
            summary = "Delete a file",
            description = "Delete a file by category and filename"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<BaseResponse<Void>> deleteFile(
            @Parameter(description = "File category", required = true)
            @PathVariable String category,
            @Parameter(description = "Filename to delete", required = true)
            @PathVariable String filename) {

        log.info("Received delete request for file: {} in category: {}", filename, category);

        fileStorageService.deleteFile(category, filename);

        return ResponseEntity.ok(BaseResponse.success("File deleted successfully", null));
    }

    @GetMapping("/list/{category}")
    @Operation(
            summary = "List files in a category",
            description = "Get a list of all files in the specified category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<BaseResponse<FileListResponse>> listFiles(
            @Parameter(description = "File category", required = true)
            @PathVariable String category) {

        log.info("Received list request for category: {}", category);

        FileListResponse response = fileStorageService.listFiles(category);

        return ResponseEntity.ok(BaseResponse.success("Files retrieved successfully", response));
    }

    @GetMapping("/presigned/{category}/{filename}")
    @Operation(
            summary = "Get presigned URL",
            description = "Generate a temporary presigned URL for secure file download"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presigned URL generated successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<BaseResponse<PresignedUrlResponse>> getPresignedUrl(
            @Parameter(description = "File category", required = true)
            @PathVariable String category,
            @Parameter(description = "Filename", required = true)
            @PathVariable String filename) {

        log.info("Received presigned URL request for file: {} in category: {}", filename, category);

        PresignedUrlResponse response = fileStorageService.getPresignedUrl(category, filename);

        return ResponseEntity.ok(BaseResponse.success("Presigned URL generated successfully", response));
    }

    @GetMapping("/exists/{category}/{filename}")
    @Operation(
            summary = "Check if file exists",
            description = "Check if a file exists in the specified category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<BaseResponse<Boolean>> fileExists(
            @Parameter(description = "File category", required = true)
            @PathVariable String category,
            @Parameter(description = "Filename to check", required = true)
            @PathVariable String filename) {

        log.info("Received exists check for file: {} in category: {}", filename, category);

        boolean exists = fileStorageService.fileExists(category, filename);

        return ResponseEntity.ok(BaseResponse.success(
                exists ? "File exists" : "File does not exist", exists));
    }
}
