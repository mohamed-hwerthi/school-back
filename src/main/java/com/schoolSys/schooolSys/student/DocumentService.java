package com.schoolSys.schooolSys.student;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentEleveRepository documentRepository;
    private final StudentRepository studentRepository;

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    @Transactional
    public DocumentEleve upload(UUID studentId, MultipartFile file, DocumentEleve.DocumentType type) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }

        try {
            Path dir = Paths.get(uploadDir, "students", String.valueOf(studentId));
            Files.createDirectories(dir);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedName = UUID.randomUUID() + extension;

            Path filePath = dir.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            DocumentEleve doc = DocumentEleve.builder()
                    .studentId(studentId)
                    .type(type)
                    .fileName(originalName)
                    .filePath(filePath.toString())
                    .contentType(file.getContentType())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentEleve> listByStudent(UUID studentId) {
        return documentRepository.findByStudentIdOrderByUploadedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public DocumentEleve findById(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", docId));
    }

    @Transactional
    public void delete(UUID docId) {
        DocumentEleve doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", docId));

        try {
            Path path = Paths.get(doc.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log but don't fail if file is already missing
        }

        documentRepository.deleteById(docId);
    }
}
