package com.schoolSys.schooolSys.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class that enables {@link StorageProperties} and
 * ensures the uploads directory exists at startup.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties storageProperties;

    /**
     * Creates the base uploads directory on application startup
     * if it does not already exist.
     */
    @PostConstruct
    public void init() {
        if ("local".equalsIgnoreCase(storageProperties.getType())) {
            Path uploadPath = Paths.get(storageProperties.getLocalPath());
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                    log.info("Répertoire de stockage créé: {}", uploadPath.toAbsolutePath());
                } catch (IOException e) {
                    throw new StorageException(
                            "Impossible de créer le répertoire de stockage: " + uploadPath, e
                    );
                }
            } else {
                log.info("Répertoire de stockage existant: {}", uploadPath.toAbsolutePath());
            }
        }
    }
}
