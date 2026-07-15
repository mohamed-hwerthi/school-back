package com.schoolSys.schooolSys.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3", matchIfMissing = true)
@RequiredArgsConstructor
public class MinIOStorageService implements StorageService {

    private final StorageProperties properties;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(properties.getS3Endpoint())
                .credentials(properties.getS3AccessKey(), properties.getS3SecretKey())
                .build();

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getS3Bucket()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getS3Bucket()).build()
                );
                log.info("Bucket MinIO créé: {}", properties.getS3Bucket());
            } else {
                log.info("Bucket MinIO existant: {}", properties.getS3Bucket());
            }
        } catch (Exception e) {
            throw new StorageException("Impossible d'initialiser le bucket MinIO: " + properties.getS3Bucket(), e);
        }

        log.info("MinIO client initialisé avec endpoint: {}", properties.getS3Endpoint());
    }

    @Override
    public FileInfo store(MultipartFile file, String folder) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
        );

        if (originalName.contains("..")) {
            throw new StorageException("Le nom du fichier contient un chemin invalide: " + originalName);
        }

        String uniqueName = UUID.randomUUID() + "_" + originalName;
        String objectName = folder + "/" + uniqueName;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getS3Bucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Impossible de stocker le fichier: " + originalName, e);
        }

        log.info("Fichier stocké dans MinIO: {} ({} octets)", objectName, file.getSize());

        return FileInfo.builder()
                .fileName(uniqueName)
                .originalName(originalName)
                .filePath(objectName)
                .fileUrl(getUrl(objectName))
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public byte[] load(String filePath) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getS3Bucket())
                        .object(filePath)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("Impossible de lire le fichier: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getS3Bucket())
                            .object(filePath)
                            .build()
            );
            log.info("Fichier supprimé de MinIO: {}", filePath);
        } catch (Exception e) {
            throw new StorageException("Impossible de supprimer le fichier: " + filePath, e);
        }
    }

    @Override
    public String getUrl(String filePath) {
        return "/api/files/" + filePath;
    }

    @Override
    public String generateDirectUrl(String filePath) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.getS3Bucket())
                            .object(filePath)
                            .method(Method.GET)
                            .expiry(60 * 60) // 1 hour
                            .build()
            );
            // Replace internal Docker hostname with public URL for browser access
            String publicUrl = properties.getS3PublicUrl();
            if (!publicUrl.isEmpty()) {
                url = url.replace(properties.getS3Endpoint(), publicUrl);
            }
            return url;
        } catch (Exception e) {
            log.warn("Impossible de générer une URL directe, fallback proxy: {}", e.getMessage());
            return getUrl(filePath);
        }
    }

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
