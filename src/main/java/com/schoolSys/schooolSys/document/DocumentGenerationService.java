package com.schoolSys.schooolSys.document;

import java.util.UUID;

import com.schoolSys.schooolSys.document.dto.DocumentHistoryDTO;
import com.schoolSys.schooolSys.document.dto.DocumentTemplateConfig;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DocumentGenerationService {

    private final DocumentGenereRepository documentRepository;
    private final StudentRepository studentRepository;
    private final DocumentTemplateService templateService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public byte[] generateCertificatScolarite(UUID eleveId) {
        Student student = findStudent(eleveId);
        DocumentTemplateConfig config = templateService.getTemplateConfig();

        String html = buildHtmlDocument(config, "Certificat de Scolarite",
                "<p>Le Directeur de l'etablissement <strong>" + config.getSchoolName() + "</strong> " +
                "certifie que l'eleve :</p>" +
                "<p style='text-align:center; font-size:18px; margin:20px 0;'><strong>" +
                student.getLastName() + " " + student.getFirstName() + "</strong></p>" +
                "<p>Matricule : " + orDefault(student.getMatricule(), "N/A") + "</p>" +
                "<p>Classe : " + orDefault(student.getClasse(), "N/A") + "</p>" +
                "<p>Niveau : " + orDefault(student.getNiveau(), "N/A") + "</p>" +
                "<p>est regulierement inscrit(e) dans notre etablissement pour l'annee scolaire en cours.</p>" +
                "<p style='margin-top:30px;'>Fait a " + extractCity(config.getAddress()) +
                ", le " + LocalDate.now().format(DATE_FMT) + "</p>" +
                "<p style='text-align:right; margin-top:40px;'>Le Directeur<br/><strong>" +
                config.getDirectorName() + "</strong></p>"
        );

        String fileName = "certificat_scolarite_" + student.getMatricule() + "_" + System.currentTimeMillis() + ".html";
        saveRecord(DocumentGenere.TypeDocument.CERTIFICAT_SCOLARITE, eleveId, fileName, null);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] generateCarteScolaire(UUID eleveId) {
        Student student = findStudent(eleveId);
        DocumentTemplateConfig config = templateService.getTemplateConfig();

        String html = buildHtmlDocument(config, "Carte Scolaire",
                "<div style='border:2px solid #333; padding:20px; max-width:400px; margin:0 auto;'>" +
                "<h3 style='text-align:center;'>" + config.getSchoolName() + "</h3>" +
                "<hr/>" +
                "<p><strong>Nom :</strong> " + student.getLastName() + "</p>" +
                "<p><strong>Prenom :</strong> " + student.getFirstName() + "</p>" +
                "<p><strong>Matricule :</strong> " + orDefault(student.getMatricule(), "N/A") + "</p>" +
                "<p><strong>Classe :</strong> " + orDefault(student.getClasse(), "N/A") + "</p>" +
                "<p><strong>Niveau :</strong> " + orDefault(student.getNiveau(), "N/A") + "</p>" +
                "<p><strong>Date de naissance :</strong> " +
                (student.getDateOfBirth() != null ? student.getDateOfBirth().format(DATE_FMT) : "N/A") + "</p>" +
                "</div>"
        );

        String fileName = "carte_scolaire_" + student.getMatricule() + "_" + System.currentTimeMillis() + ".html";
        saveRecord(DocumentGenere.TypeDocument.CARTE_SCOLAIRE, eleveId, fileName, null);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] generateAttestationReussite(UUID eleveId, String anneeScolaire) {
        Student student = findStudent(eleveId);
        DocumentTemplateConfig config = templateService.getTemplateConfig();

        String html = buildHtmlDocument(config, "Attestation de Reussite",
                "<p>Le Directeur de l'etablissement <strong>" + config.getSchoolName() + "</strong> " +
                "atteste que l'eleve :</p>" +
                "<p style='text-align:center; font-size:18px; margin:20px 0;'><strong>" +
                student.getLastName() + " " + student.getFirstName() + "</strong></p>" +
                "<p>Matricule : " + orDefault(student.getMatricule(), "N/A") + "</p>" +
                "<p>a reussi avec succes l'annee scolaire <strong>" + orDefault(anneeScolaire, "en cours") +
                "</strong> en classe de <strong>" + orDefault(student.getClasse(), "N/A") + "</strong>.</p>" +
                "<p style='margin-top:30px;'>Fait a " + extractCity(config.getAddress()) +
                ", le " + LocalDate.now().format(DATE_FMT) + "</p>" +
                "<p style='text-align:right; margin-top:40px;'>Le Directeur<br/><strong>" +
                config.getDirectorName() + "</strong></p>"
        );

        String fileName = "attestation_reussite_" + student.getMatricule() + "_" + System.currentTimeMillis() + ".html";
        saveRecord(DocumentGenere.TypeDocument.ATTESTATION_REUSSITE, eleveId, fileName, anneeScolaire);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] generateReleveNotes(UUID eleveId, Integer trimestre) {
        Student student = findStudent(eleveId);
        DocumentTemplateConfig config = templateService.getTemplateConfig();

        String trimestreLabel = trimestre != null ? "Trimestre " + trimestre : "Annuel";

        String html = buildHtmlDocument(config, "Releve de Notes - " + trimestreLabel,
                "<p style='text-align:center; font-size:16px;'><strong>Releve de Notes</strong></p>" +
                "<p><strong>Eleve :</strong> " + student.getLastName() + " " + student.getFirstName() + "</p>" +
                "<p><strong>Matricule :</strong> " + orDefault(student.getMatricule(), "N/A") + "</p>" +
                "<p><strong>Classe :</strong> " + orDefault(student.getClasse(), "N/A") + "</p>" +
                "<p><strong>Periode :</strong> " + trimestreLabel + "</p>" +
                "<table style='width:100%; border-collapse:collapse; margin-top:15px;'>" +
                "<thead><tr style='background:#f0f0f0;'>" +
                "<th style='border:1px solid #ccc; padding:8px; text-align:left;'>Matiere</th>" +
                "<th style='border:1px solid #ccc; padding:8px; text-align:center;'>Note</th>" +
                "<th style='border:1px solid #ccc; padding:8px; text-align:center;'>Coefficient</th>" +
                "<th style='border:1px solid #ccc; padding:8px; text-align:center;'>Observation</th>" +
                "</tr></thead>" +
                "<tbody>" +
                "<tr><td colspan='4' style='border:1px solid #ccc; padding:8px; text-align:center; color:#999;'>" +
                "Les notes seront remplies par le systeme de gestion des notes</td></tr>" +
                "</tbody></table>" +
                "<p style='margin-top:30px;'>Fait le " + LocalDate.now().format(DATE_FMT) + "</p>" +
                "<p style='text-align:right; margin-top:40px;'>Le Directeur<br/><strong>" +
                config.getDirectorName() + "</strong></p>"
        );

        String fileName = "releve_notes_" + student.getMatricule() + "_T" + (trimestre != null ? trimestre : "A") + "_" + System.currentTimeMillis() + ".html";
        DocumentGenere record = saveRecord(DocumentGenere.TypeDocument.RELEVE_NOTES, eleveId, fileName, null);
        record.setTrimestre(trimestre);
        documentRepository.save(record);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] generateRecuPaiement(UUID paiementId) {
        DocumentTemplateConfig config = templateService.getTemplateConfig();

        String html = buildHtmlDocument(config, "Recu de Paiement",
                "<p style='text-align:center; font-size:16px;'><strong>Recu de Paiement</strong></p>" +
                "<p><strong>Numero de paiement :</strong> #" + paiementId + "</p>" +
                "<p><strong>Date :</strong> " + LocalDate.now().format(DATE_FMT) + "</p>" +
                "<hr/>" +
                "<p>Les details du paiement seront remplis par le module de finance.</p>" +
                "<hr/>" +
                "<p style='margin-top:20px; font-style:italic;'>" + config.getFooterText() + "</p>" +
                "<p style='text-align:right; margin-top:30px;'>Signature<br/><strong>" +
                config.getDirectorName() + "</strong></p>"
        );

        String fileName = "recu_paiement_" + paiementId + "_" + System.currentTimeMillis() + ".html";
        saveRecord(DocumentGenere.TypeDocument.RECU_PAIEMENT, null, fileName, null);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public byte[] generateBulk(List<UUID> eleveIds, String type) {
        DocumentGenere.TypeDocument typeDoc;
        try {
            typeDoc = DocumentGenere.TypeDocument.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type de document invalide: " + type);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (UUID eleveId : eleveIds) {
                byte[] content;
                switch (typeDoc) {
                    case CERTIFICAT_SCOLARITE -> content = generateCertificatScolarite(eleveId);
                    case CARTE_SCOLAIRE -> content = generateCarteScolaire(eleveId);
                    case ATTESTATION_REUSSITE -> content = generateAttestationReussite(eleveId, null);
                    case RELEVE_NOTES -> content = generateReleveNotes(eleveId, null);
                    default -> throw new IllegalArgumentException("Type non supporte pour bulk: " + type);
                }

                Student student = findStudent(eleveId);
                String entryName = type.toLowerCase() + "_" + student.getMatricule() + "_" + eleveId + ".html";
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(content);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la generation bulk de documents", e);
            throw new RuntimeException("Erreur lors de la generation en masse", e);
        }
    }

    public List<DocumentHistoryDTO> getHistory() {
        return documentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private Student findStudent(UUID eleveId) {
        return studentRepository.findById(eleveId)
                .orElseThrow(() -> new ResourceNotFoundException("Eleve non trouve avec l'id: " + eleveId));
    }

    private DocumentGenere saveRecord(DocumentGenere.TypeDocument type, UUID eleveId, String fileName, String anneeScolaire) {
        DocumentGenere doc = DocumentGenere.builder()
                .typeDocument(type)
                .eleveId(eleveId)
                .fileName(fileName)
                .anneeScolaire(anneeScolaire)
                .createdAt(LocalDateTime.now())
                .build();
        return documentRepository.save(doc);
    }

    private DocumentHistoryDTO toHistoryDTO(DocumentGenere doc) {
        String eleveName = null;
        if (doc.getEleveId() != null) {
            eleveName = studentRepository.findById(doc.getEleveId())
                    .map(s -> s.getLastName() + " " + s.getFirstName())
                    .orElse("Eleve #" + doc.getEleveId());
        }

        return DocumentHistoryDTO.builder()
                .id(doc.getId())
                .type(doc.getTypeDocument())
                .eleveName(eleveName)
                .fileName(doc.getFileName())
                .dateGeneration(doc.getCreatedAt())
                .anneeScolaire(doc.getAnneeScolaire())
                .trimestre(doc.getTrimestre())
                .build();
    }

    private String buildHtmlDocument(DocumentTemplateConfig config, String title, String body) {
        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'/>" +
                "<title>" + title + "</title>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 40px; color: #333; }" +
                "h1, h2, h3 { color: #1a365d; }" +
                ".header { text-align: center; border-bottom: 2px solid #1a365d; padding-bottom: 15px; margin-bottom: 25px; }" +
                ".footer { text-align: center; margin-top: 40px; padding-top: 15px; border-top: 1px solid #ccc; font-size: 12px; color: #666; }" +
                "table { width: 100%; }" +
                "@media print { body { padding: 20px; } }" +
                "</style></head><body>" +
                "<div class='header'>" +
                (config.getSchoolLogo() != null && !config.getSchoolLogo().isEmpty()
                        ? "<img src='" + config.getSchoolLogo() + "' alt='Logo' style='max-height:60px;'/><br/>" : "") +
                "<h2>" + config.getSchoolName() + "</h2>" +
                "<p>" + orDefault(config.getHeaderText(), "") + "</p>" +
                "<p>" + orDefault(config.getAddress(), "") + "</p>" +
                "</div>" +
                "<h2 style='text-align:center;'>" + title + "</h2>" +
                body +
                "<div class='footer'>" + orDefault(config.getFooterText(), "") + "</div>" +
                "</body></html>";
    }

    private String extractCity(String address) {
        if (address == null || address.isEmpty()) return "...";
        String[] parts = address.split(",");
        return parts[parts.length - 1].trim();
    }

    private String orDefault(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
}
