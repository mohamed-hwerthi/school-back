package com.schoolSys.schooolSys.student;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.storage.FileInfo;
import com.schoolSys.schooolSys.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentEleveRepository documentRepository;
    private final StudentRepository studentRepository;
    private final StorageService storageService;

    @Transactional
    public DocumentEleve upload(UUID studentId, MultipartFile file, DocumentEleve.DocumentType type) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }

        FileInfo info = storageService.store(file, "students/" + studentId);

        DocumentEleve doc = DocumentEleve.builder()
                .studentId(studentId)
                .type(type)
                .fileName(info.getOriginalName())
                .filePath(info.getFilePath())
                .contentType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .build();

        return documentRepository.save(doc);
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
            storageService.delete(doc.getFilePath());
        } catch (Exception e) {
            log.warn("Impossible de supprimer le fichier {}: {}", doc.getFilePath(), e.getMessage());
        }

        documentRepository.deleteById(docId);
    }

    public byte[] downloadContent(UUID docId) {
        DocumentEleve doc = findById(docId);
        return storageService.load(doc.getFilePath());
    }
}
