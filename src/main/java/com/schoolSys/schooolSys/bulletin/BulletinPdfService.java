package com.schoolSys.schooolSys.bulletin;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.settings.SchoolSettings;
import com.schoolSys.schooolSys.settings.SchoolSettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Génère un PDF à partir d'un {@link BulletinDTO} via OpenHTMLToPDF.
 * La police arabe Amiri est chargée depuis {@code resources/fonts/} et
 * fournie au moteur de rendu pour que le texte arabe RTL s'affiche
 * correctement dans le PDF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinPdfService {

    private final SchoolSettingsRepository schoolSettingsRepository;

    private Path amiriRegular;
    private Path amiriBold;

    @PostConstruct
    void init() throws IOException {
        // OpenHTMLToPDF utilise JUL : on coupe les logs INFO trop verbeux.
        XRLog.setLevel("com.openhtmltopdf.css-parse", Level.SEVERE);
        XRLog.setLevel("com.openhtmltopdf.layout", Level.SEVERE);
        XRLog.setLevel("com.openhtmltopdf.match", Level.SEVERE);
        // Les polices doivent être sur le système de fichiers pour OpenHTMLToPDF.
        amiriRegular = copyResourceToTemp("fonts/Amiri-Regular.ttf");
        amiriBold = copyResourceToTemp("fonts/Amiri-Bold.ttf");
    }

    private Path copyResourceToTemp(String resourcePath) throws IOException {
        try (InputStream in = new ClassPathResource(resourcePath).getInputStream()) {
            Path tmp = Files.createTempFile("schoolsys-", "-" + resourcePath.replaceAll(".*/", ""));
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }

    /** Sérialise un {@link BulletinDTO} en PDF (octets) prêt à l'envoi. */
    public byte[] toPdf(BulletinDTO bulletin) {
        SchoolSettings settings = schoolSettingsRepository.findAll()
                .stream().findFirst().orElse(null);
        String html = BulletinHtmlTemplate.render(bulletin, settings);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(amiriRegular.toFile(), "Amiri",
                    400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(amiriBold.toFile(), "Amiri",
                    700, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Erreur génération PDF bulletin {}", bulletin.getStudentId(), e);
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }
}
