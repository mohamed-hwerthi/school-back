package com.schoolSys.schooolSys.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing metadata about an uploaded file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /** Generated unique file name on disk. */
    private String fileName;

    /** Original file name as uploaded by the user. */
    private String originalName;

    /** Relative path of the file within the storage root. */
    private String filePath;

    /** Public URL to access the file. */
    private String fileUrl;

    /** MIME content type. */
    private String contentType;

    /** File size in bytes. */
    private long size;

    /** Timestamp of the upload. */
    private LocalDateTime uploadedAt;
}
