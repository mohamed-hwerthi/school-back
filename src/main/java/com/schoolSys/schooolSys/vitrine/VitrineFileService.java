package com.schoolSys.schooolSys.vitrine;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class VitrineFileService {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    /**
     * Uploads an image for the current tenant's vitrine.
     *
     * @param file the uploaded file
     * @return the relative URL path to the stored image
     */
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Le fichier dépasse 5 Mo");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Formats acceptés: JPEG, PNG, GIF, WebP, SVG");
        }

        try {
            String tenant = TenantContext.getCurrentTenant();
            Path dir = Paths.get(uploadDir, "vitrine", tenant);
            Files.createDirectories(dir);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedName = UUID.randomUUID() + extension;

            Path filePath = dir.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return a URL path the frontend can use
            return "/uploads/vitrine/" + tenant + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }
}
