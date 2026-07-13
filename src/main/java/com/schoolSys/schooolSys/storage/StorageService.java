package com.schoolSys.schooolSys.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for file storage operations.
 * Implementations may use local disk, S3, or any other backend.
 */
public interface StorageService {

    /**
     * Stores a file in the given folder.
     *
     * @param file   the multipart file to store
     * @param folder the logical folder (e.g. "students", "devoirs")
     * @return metadata about the stored file
     */
    FileInfo store(MultipartFile file, String folder);

    /**
     * Loads a file's bytes by its relative path.
     *
     * @param filePath the relative path within storage
     * @return the file content as a byte array
     */
    byte[] load(String filePath);

    /**
     * Deletes a file by its relative path.
     *
     * @param filePath the relative path within storage
     */
    void delete(String filePath);

    /**
     * Returns the URL to access a stored file.
     *
     * @param filePath the relative path within storage
     * @return the URL string
     */
    String getUrl(String filePath);
}
