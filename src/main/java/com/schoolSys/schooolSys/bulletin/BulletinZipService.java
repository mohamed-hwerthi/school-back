package com.schoolSys.schooolSys.bulletin;

import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.settings.SchoolSettings;
import com.schoolSys.schooolSys.settings.SchoolSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Sérialise tous les bulletins d'une (classe, trimestre, version) dans un
 * flux ZIP. Chaque PDF s'appelle {@code <nom>_<matricule>_<annee>.pdf}.
 * Streaming : écrit directement dans le {@link OutputStream} fourni, sans
 * monter en mémoire l'archive complète.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinZipService {

    private final BulletinService bulletinService;
    private final BulletinPdfService bulletinPdfService;
    private final SchoolSettingsRepository schoolSettingsRepository;

    public void writeZip(UUID classeId, Integer trimestre, String version,
                         OutputStream out) throws IOException {
        List<BulletinDTO> bulletins = bulletinService.getBulletins(classeId, trimestre, version);
        log.info("ZIP request: classeId={} trimestre={} version={} → {} bulletin(s)",
                classeId, trimestre, version, bulletins.size());
        SchoolSettings settings = schoolSettingsRepository.findAll()
                .stream().findFirst().orElse(null);
        String annee = settings != null && settings.getAnneeScolaire() != null
                ? sanitize(settings.getAnneeScolaire()) : "annee";

        Set<String> usedNames = new HashSet<>();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            // PDF déjà compressés en interne ; on stocke sans recompression
            // pour gagner du CPU.
            zip.setLevel(0);

            for (BulletinDTO b : bulletins) {
                byte[] pdf;
                try {
                    pdf = bulletinPdfService.toPdf(b);
                } catch (Exception e) {
                    log.error("Skip bulletin {} ({}): {}",
                            b.getStudentId(), b.getStudentName(), e.getMessage(), e);
                    continue;
                }
                String base = String.join("_",
                        sanitize(b.getStudentName()),
                        sanitize(nullSafe(b.getMatricule())),
                        annee);
                String name = uniqueName(usedNames, base + ".pdf");
                ZipEntry entry = new ZipEntry(name);
                zip.putNextEntry(entry);
                zip.write(pdf);
                zip.closeEntry();
                zip.flush();
            }
            zip.finish();
        }
    }

    private static String uniqueName(Set<String> used, String name) {
        if (used.add(name)) return name;
        int i = 2;
        String stripped = name.replaceFirst("\\.pdf$", "");
        while (true) {
            String candidate = stripped + "_" + i + ".pdf";
            if (used.add(candidate)) return candidate;
            i++;
        }
    }

    private static String sanitize(String s) {
        if (s == null || s.isBlank()) return "";
        // Caractères interdits dans les noms de fichiers Windows/macOS/Linux.
        String cleaned = s.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "")
                .replaceAll("\\s+", "_")
                .trim();
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
