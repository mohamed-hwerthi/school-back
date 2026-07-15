package com.schoolSys.schooolSys.storage;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for file storage operations.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * Upload a single file.
     * POST /api/files/upload?folder=students
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileInfo>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        FileInfo info = storageService.store(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(info));
    }

    /**
     * Upload multiple files.
     * POST /api/files/upload-multiple?folder=devoirs
     */
    @PostMapping("/upload-multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FileInfo>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        List<FileInfo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(storageService.store(file, folder));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(results));
    }

    /**
     * Download / serve a file.
     * In production, redirects to a direct URL (presigned for S3, local proxy fallback).
     * GET /api/files/{*filePath}
     */
    @GetMapping("/{*filePath}")
    public ResponseEntity<?> downloadFile(@PathVariable String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        String directUrl = storageService.generateDirectUrl(filePath);

        // If the storage returns a same-host proxy URL, serve bytes directly
        if (directUrl.startsWith("/api/files/")) {
            byte[] content = storageService.load(filePath);
            String contentType = detectContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + extractFileName(filePath) + "\"")
                    .body(content);
        }

        // Redirect to the direct URL (presigned MinIO URL or external storage)
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, directUrl)
                .build();
    }

    /**
     * Delete a file.
     * DELETE /api/files?path=students/uuid_photo.jpg
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("path") String filePath) {
        storageService.delete(filePath);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Get file metadata.
     * GET /api/files/info?path=students/uuid_photo.jpg
     */
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileInfo>> getFileInfo(@RequestParam("path") String filePath) {
        String fileName = extractFileName(filePath);
        String originalName = fileName.contains("_")
                ? fileName.substring(fileName.indexOf('_') + 1)
                : fileName;
        FileInfo info = FileInfo.builder()
                .fileName(fileName)
                .originalName(originalName)
                .filePath(filePath)
                .fileUrl(storageService.getUrl(filePath))
                .contentType(detectContentType(filePath))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    // ── Private helpers ──────────────────────────────────────────────

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "unknown";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String detectContentType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        return "application/octet-stream";
    }
}
