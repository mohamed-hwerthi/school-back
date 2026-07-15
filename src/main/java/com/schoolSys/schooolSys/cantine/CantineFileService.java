package com.schoolSys.schooolSys.cantine;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.storage.FileInfo;
import com.schoolSys.schooolSys.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CantineFileService {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final StorageService storageService;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Le fichier dépasse 5 Mo");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Formats acceptés: JPEG, PNG, GIF, WebP");
        }

        String tenant = TenantContext.getCurrentTenant();
        FileInfo info = storageService.store(file, "cantine/" + tenant);

        return info.getFileUrl();
    }
}
