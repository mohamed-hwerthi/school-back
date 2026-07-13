package com.schoolSys.schooolSys.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for file storage.
 * Supports local filesystem and S3-compatible storage.
 */
@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Storage type: "local" or "s3". */
    private String type = "local";

    /** Base directory for local file storage. */
    private String localPath = "./uploads";

    /** S3 bucket name. */
    private String s3Bucket = "";

    /** S3 region. */
    private String s3Region = "";

    /** S3 endpoint (for MinIO compatibility). */
    private String s3Endpoint = "";

    /** S3 access key. */
    private String s3AccessKey = "";

    /** S3 secret key. */
    private String s3SecretKey = "";

    /** Maximum allowed file size in bytes (default 10 MB). */
    private long maxFileSize = 10L * 1024 * 1024;

    /** List of allowed file extensions. */
    private List<String> allowedExtensions = List.of(
            "pdf", "jpg", "jpeg", "png", "gif",
            "doc", "docx", "xls", "xlsx",
            "mp4", "mp3"
    );
}
