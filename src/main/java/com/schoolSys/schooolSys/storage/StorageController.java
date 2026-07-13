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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
    private final StorageProperties storageProperties;

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
     * GET /api/files/{*filePath}
     */
    @GetMapping("/{*filePath}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filePath) {
        // Strip leading slash added by Spring's {*path} capture
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        byte[] content = storageService.load(filePath);

        // Detect content type from extension
        String contentType = detectContentType(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + extractFileName(filePath) + "\"")
                .body(content);
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
        Path path = Paths.get(storageProperties.getLocalPath()).resolve(filePath).normalize();
        if (!Files.exists(path)) {
            throw new StorageException("Fichier non trouvé: " + filePath);
        }

        try {
            String originalName = extractFileName(filePath);
            FileInfo info = FileInfo.builder()
                    .fileName(extractFileName(filePath))
                    .originalName(originalName.contains("_")
                            ? originalName.substring(originalName.indexOf('_') + 1)
                            : originalName)
                    .filePath(filePath)
                    .fileUrl(storageService.getUrl(filePath))
                    .contentType(detectContentType(filePath))
                    .size(Files.size(path))
                    .uploadedAt(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(ApiResponse.ok(info));
        } catch (Exception e) {
            throw new StorageException("Impossible de lire les métadonnées: " + filePath, e);
        }
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
