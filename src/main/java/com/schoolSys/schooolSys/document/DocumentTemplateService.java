package com.schoolSys.schooolSys.document;

import com.schoolSys.schooolSys.document.dto.DocumentTemplateConfig;
import org.springframework.stereotype.Service;

/**
 * Manages document template configuration (school branding, signatures, etc.).
 * Currently stored in-memory; can be persisted to school_settings later.
 */
@Service
public class DocumentTemplateService {

    private DocumentTemplateConfig config = DocumentTemplateConfig.builder()
            .schoolName("Ecole Exemple")
            .schoolLogo("")
            .address("123 Rue de l'Ecole, Ville")
            .directorName("M. Directeur")
            .signatures("")
            .headerText("Etablissement Scolaire Prive")
            .footerText("Document officiel genere electroniquement")
            .build();

    public DocumentTemplateConfig getTemplateConfig() {
        return config;
    }

    public DocumentTemplateConfig updateTemplateConfig(DocumentTemplateConfig newConfig) {
        if (newConfig.getSchoolName() != null) config.setSchoolName(newConfig.getSchoolName());
        if (newConfig.getSchoolLogo() != null) config.setSchoolLogo(newConfig.getSchoolLogo());
        if (newConfig.getAddress() != null) config.setAddress(newConfig.getAddress());
        if (newConfig.getDirectorName() != null) config.setDirectorName(newConfig.getDirectorName());
        if (newConfig.getSignatures() != null) config.setSignatures(newConfig.getSignatures());
        if (newConfig.getHeaderText() != null) config.setHeaderText(newConfig.getHeaderText());
        if (newConfig.getFooterText() != null) config.setFooterText(newConfig.getFooterText());
        return config;
    }
}
