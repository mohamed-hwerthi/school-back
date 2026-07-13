package com.schoolSys.schooolSys.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Local filesystem implementation of {@link StorageService}.
 * Files are stored under {@code {localPath}/{folder}/{uuid}_{originalName}}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public FileInfo store(MultipartFile file, String folder) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
        );

        // Prevent directory traversal attacks
        if (originalName.contains("..")) {
            throw new StorageException("Le nom du fichier contient un chemin invalide: " + originalName);
        }

        // Generate a unique filename
        String uniqueName = UUID.randomUUID().toString() + "_" + originalName;

        // Resolve folder and create directories
        Path folderPath = Paths.get(properties.getLocalPath()).resolve(folder).normalize();
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new StorageException("Impossible de créer le répertoire: " + folderPath, e);
        }

        // Copy file to destination
        Path destination = folderPath.resolve(uniqueName).normalize();
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Impossible de stocker le fichier: " + originalName, e);
        }

        // Build relative path for storage reference
        String relativePath = folder + "/" + uniqueName;

        log.info("Fichier stocké: {} ({} octets)", relativePath, file.getSize());

        return FileInfo.builder()
                .fileName(uniqueName)
                .originalName(originalName)
                .filePath(relativePath)
                .fileUrl(getUrl(relativePath))
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public byte[] load(String filePath) {
        Path path = Paths.get(properties.getLocalPath()).resolve(filePath).normalize();
        if (!Files.exists(path)) {
            throw new StorageException("Fichier non trouvé: " + filePath);
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new StorageException("Impossible de lire le fichier: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        Path path = Paths.get(properties.getLocalPath()).resolve(filePath).normalize();
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Fichier supprimé: {}", filePath);
            } else {
                log.warn("Fichier déjà absent: {}", filePath);
            }
        } catch (IOException e) {
            throw new StorageException("Impossible de supprimer le fichier: " + filePath, e);
        }
    }

    @Override
    public String getUrl(String filePath) {
        return "/api/files/" + filePath;
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Le fichier est vide.");
        }

        if (file.getSize() > properties.getMaxFileSize()) {
            throw new StorageException(
                    "Le fichier dépasse la taille maximale autorisée ("
                            + (properties.getMaxFileSize() / (1024 * 1024)) + " Mo)."
            );
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (!properties.getAllowedExtensions().contains(extension)) {
                throw new StorageException(
                        "Extension de fichier non autorisée: ." + extension
                                + ". Extensions autorisées: " + properties.getAllowedExtensions()
                );
            }
        }
    }
}
