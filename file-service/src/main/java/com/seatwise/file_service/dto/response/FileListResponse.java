package com.seatwise.file_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    private String category;
    private int totalFiles;
    private List<FileInfo> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String filename;
        private String path;
        private long size;
        private String sizeFormatted;
        private String lastModified;
    }
}
