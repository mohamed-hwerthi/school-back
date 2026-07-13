package com.schoolSys.schooolSys.ai;

import com.schoolSys.schooolSys.ai.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiConfig aiConfig;

    /**
     * Generate a bulletin comment for a student.
     */
    public AiCommentResponse generateBulletinComment(AiCommentRequest request) {
        if (!aiConfig.isEnabled()) {
            return defaultBulletinComment(request);
        }

        String toneInstruction = switch (request.getTone()) {
            case ENCOURAGEANT -> "Utilise un ton encourageant et positif.";
            case STRICT -> "Utilise un ton strict et exigeant.";
            default -> "Utilise un ton neutre et professionnel.";
        };

        String prompt = String.format(
                "Tu es un enseignant qui redige des commentaires pour les bulletins scolaires. " +
                "%s " +
                "Eleve: %s, Moyenne: %.2f/20. " +
                "Details des notes: %s. " +
                "Redige un commentaire de bulletin de 2-3 phrases maximum en francais. " +
                "Puis donne 2-3 suggestions d'amelioration sous forme de liste.",
                toneInstruction,
                request.getStudentName(),
                request.getMoyenne() != null ? request.getMoyenne() : 0.0,
                request.getNoteDetails() != null ? String.join(", ", request.getNoteDetails()) : "Aucun detail"
        );

        String aiResponse = callAiApi(prompt);

        // Parse response - split comment from suggestions
        String comment = aiResponse;
        List<String> suggestions = new ArrayList<>();

        if (aiResponse.contains("\n-") || aiResponse.contains("\n*")) {
            String[] parts = aiResponse.split("\n\n", 2);
            comment = parts[0].trim();
            if (parts.length > 1) {
                String[] lines = parts[1].split("\n");
                for (String line : lines) {
                    String cleaned = line.replaceAll("^[-*]\\s*", "").trim();
                    if (!cleaned.isEmpty()) {
                        suggestions.add(cleaned);
                    }
                }
            }
        }

        return AiCommentResponse.builder()
                .comment(comment)
                .suggestions(suggestions)
                .build();
    }

    /**
     * Generate a performance summary for a student.
     */
    public String generatePerformanceSummary(AiPerformanceRequest request) {
        if (!aiConfig.isEnabled()) {
            return defaultPerformanceSummary(request);
        }

        String prompt = String.format(
                "Tu es un conseiller pedagogique. Redige un resume de performance pour l'eleve %s. " +
                "Moyenne: %.2f/20. Absences: %d. Retards: %d. " +
                "Details: %s. " +
                "Fais un resume de 3-4 phrases en francais avec des recommandations.",
                request.getStudentName(),
                request.getMoyenne() != null ? request.getMoyenne() : 0.0,
                request.getTotalAbsences(),
                request.getTotalRetards(),
                request.getNoteDetails() != null ? String.join(", ", request.getNoteDetails()) : "Aucun detail"
        );

        return callAiApi(prompt);
    }

    /**
     * Detect anomalies in student data (grade drops, high absences, patterns).
     */
    public List<AnomalyDTO> detectAnomalies(AiDetectAnomaliesRequest request) {
        if (!aiConfig.isEnabled()) {
            return defaultAnomalyDetection(request);
        }

        String prompt = String.format(
                "Analyse les donnees suivantes et detecte les anomalies. " +
                "Notes: %s. Absences par mois: %s. " +
                "Pour chaque anomalie, indique: type (GRADE_DROP, HIGH_ABSENCE, BEHAVIOR_PATTERN), " +
                "description, severite (LOW, MEDIUM, HIGH). " +
                "Reponds uniquement en JSON sous la forme [{\"type\":\"...\",\"description\":\"...\",\"severity\":\"...\"}]",
                request.getNotes() != null ? request.getNotes().toString() : "[]",
                request.getAbsences() != null ? request.getAbsences().toString() : "[]"
        );

        String aiResponse = callAiApi(prompt);

        // Parse JSON response - simplified parsing
        List<AnomalyDTO> anomalies = new ArrayList<>();
        try {
            // Simple parsing without Jackson dependency
            if (aiResponse.contains("GRADE_DROP")) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyDTO.AnomalyType.GRADE_DROP)
                        .description(extractDescription(aiResponse, "GRADE_DROP"))
                        .severity("MEDIUM")
                        .studentId(request.getStudentId())
                        .build());
            }
            if (aiResponse.contains("HIGH_ABSENCE")) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyDTO.AnomalyType.HIGH_ABSENCE)
                        .description(extractDescription(aiResponse, "HIGH_ABSENCE"))
                        .severity("HIGH")
                        .studentId(request.getStudentId())
                        .build());
            }
            if (aiResponse.contains("BEHAVIOR_PATTERN")) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyDTO.AnomalyType.BEHAVIOR_PATTERN)
                        .description(extractDescription(aiResponse, "BEHAVIOR_PATTERN"))
                        .severity("MEDIUM")
                        .studentId(request.getStudentId())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI anomaly response: {}", e.getMessage());
            // Fall back to rule-based detection
            return defaultAnomalyDetection(request);
        }

        if (anomalies.isEmpty()) {
            return defaultAnomalyDetection(request);
        }

        return anomalies;
    }

    // ===================== AI API Call =====================

    private String callAiApi(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url;
            Map<String, Object> body;

            if ("openai".equalsIgnoreCase(aiConfig.getProvider())) {
                url = "https://api.openai.com/v1/chat/completions";
                headers.setBearerAuth(aiConfig.getApiKey());

                body = Map.of(
                        "model", aiConfig.getModel(),
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "max_tokens", aiConfig.getMaxTokens()
                );
            } else {
                // Anthropic (default)
                url = "https://api.anthropic.com/v1/messages";
                headers.set("x-api-key", aiConfig.getApiKey());
                headers.set("anthropic-version", "2023-06-01");

                body = Map.of(
                        "model", aiConfig.getModel(),
                        "max_tokens", aiConfig.getMaxTokens(),
                        "messages", List.of(Map.of("role", "user", "content", prompt))
                );
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null) {
                if ("openai".equalsIgnoreCase(aiConfig.getProvider())) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get(0).get("text");
                    }
                }
            }

            return "Reponse IA non disponible.";

        } catch (Exception e) {
            log.error("AI API call failed: {}", e.getMessage());
            return "Service IA temporairement indisponible.";
        }
    }

    // ===================== Default (fallback) implementations =====================

    private AiCommentResponse defaultBulletinComment(AiCommentRequest request) {
        double moyenne = request.getMoyenne() != null ? request.getMoyenne() : 0.0;
        String comment;
        List<String> suggestions = new ArrayList<>();

        if (moyenne >= 16) {
            comment = String.format("Excellent trimestre pour %s avec une moyenne de %.2f/20. Travail serieux et regulier. Felicitations!",
                    request.getStudentName(), moyenne);
            suggestions.add("Continuer sur cette lancee");
            suggestions.add("Envisager des activites d'approfondissement");
        } else if (moyenne >= 12) {
            comment = String.format("Bon trimestre pour %s avec une moyenne de %.2f/20. Des resultats satisfaisants qui montrent un engagement positif.",
                    request.getStudentName(), moyenne);
            suggestions.add("Renforcer les matieres les plus faibles");
            suggestions.add("Maintenir l'effort et la regularite");
        } else if (moyenne >= 10) {
            comment = String.format("Trimestre passable pour %s avec une moyenne de %.2f/20. Des efforts supplementaires sont necessaires pour progresser.",
                    request.getStudentName(), moyenne);
            suggestions.add("Mettre en place un plan de travail regulier");
            suggestions.add("Solliciter de l'aide aupres des enseignants");
            suggestions.add("Revoir les bases dans les matieres en difficulte");
        } else {
            comment = String.format("Trimestre insuffisant pour %s avec une moyenne de %.2f/20. Un sursaut est necessaire pour rattraper le retard.",
                    request.getStudentName(), moyenne);
            suggestions.add("Rendez-vous parent-enseignant recommande");
            suggestions.add("Mise en place d'un soutien scolaire");
            suggestions.add("Travail quotidien et revisions regulieres indispensables");
        }

        return AiCommentResponse.builder()
                .comment(comment)
                .suggestions(suggestions)
                .build();
    }

    private String defaultPerformanceSummary(AiPerformanceRequest request) {
        double moyenne = request.getMoyenne() != null ? request.getMoyenne() : 0.0;
        String level = moyenne >= 16 ? "excellent" : moyenne >= 12 ? "satisfaisant" : moyenne >= 10 ? "passable" : "insuffisant";

        return String.format(
                "Resume de performance pour %s : Niveau global %s avec une moyenne de %.2f/20. " +
                "L'eleve a cumule %d absence(s) et %d retard(s) sur la periode. " +
                "%s",
                request.getStudentName(), level, moyenne,
                request.getTotalAbsences(), request.getTotalRetards(),
                request.getTotalAbsences() > 10
                        ? "Le taux d'absence eleve necessite une attention particuliere."
                        : "La presence est dans les normes attendues."
        );
    }

    private List<AnomalyDTO> defaultAnomalyDetection(AiDetectAnomaliesRequest request) {
        List<AnomalyDTO> anomalies = new ArrayList<>();

        // Rule-based anomaly detection
        if (request.getNotes() != null && request.getNotes().size() >= 2) {
            List<Double> notes = request.getNotes();
            for (int i = 1; i < notes.size(); i++) {
                if (notes.get(i - 1) - notes.get(i) >= 4.0) {
                    anomalies.add(AnomalyDTO.builder()
                            .type(AnomalyDTO.AnomalyType.GRADE_DROP)
                            .description(String.format("Chute de note significative : de %.1f a %.1f", notes.get(i - 1), notes.get(i)))
                            .severity("MEDIUM")
                            .studentId(request.getStudentId())
                            .build());
                }
            }
        }

        if (request.getAbsences() != null) {
            int totalAbsences = request.getAbsences().stream().mapToInt(Integer::intValue).sum();
            if (totalAbsences > 15) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyDTO.AnomalyType.HIGH_ABSENCE)
                        .description(String.format("Nombre total d'absences eleve : %d", totalAbsences))
                        .severity("HIGH")
                        .studentId(request.getStudentId())
                        .build());
            }

            // Detect increasing trend
            if (request.getAbsences().size() >= 3) {
                List<Integer> abs = request.getAbsences();
                boolean increasing = true;
                for (int i = 1; i < abs.size(); i++) {
                    if (abs.get(i) <= abs.get(i - 1)) {
                        increasing = false;
                        break;
                    }
                }
                if (increasing) {
                    anomalies.add(AnomalyDTO.builder()
                            .type(AnomalyDTO.AnomalyType.BEHAVIOR_PATTERN)
                            .description("Tendance a la hausse des absences detectee")
                            .severity("MEDIUM")
                            .studentId(request.getStudentId())
                            .build());
                }
            }
        }

        return anomalies;
    }

    private String extractDescription(String response, String type) {
        // Simple extraction - find text after the type mention
        int idx = response.indexOf(type);
        if (idx >= 0) {
            int descStart = response.indexOf("description", idx);
            if (descStart >= 0) {
                int valueStart = response.indexOf(":", descStart) + 1;
                int valueEnd = response.indexOf("\"", response.indexOf("\"", valueStart) + 1);
                if (valueStart > 0 && valueEnd > valueStart) {
                    return response.substring(response.indexOf("\"", valueStart) + 1, valueEnd).trim();
                }
            }
        }
        return "Anomalie detectee de type " + type;
    }
}
