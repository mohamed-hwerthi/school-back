package com.schoolSys.schooolSys.ai;

import com.schoolSys.schooolSys.ai.dto.AiChatRequest;
import com.schoolSys.schooolSys.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final AiConfig aiConfig;

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant intelligent integre dans un systeme de gestion scolaire.
            Tu aides les utilisateurs (directeurs, enseignants, secretaires) a utiliser l'application.

            Fonctionnalites disponibles dans l'application :
            - Gestion des eleves (inscription, suivi, bulletins, passages)
            - Gestion des enseignants (profils, affectations, emploi du temps)
            - Gestion des notes et examens (saisie, calcul des moyennes, bulletins)
            - Gestion financiere (paiements, depenses, caisse, remises, relances, factures)
            - Gestion des absences et retards (saisie, justification, alertes)
            - Emploi du temps (creneaux, salles, conflits)
            - Communication (messagerie, notifications, annonces)
            - Rapports et statistiques
            - Import/Export de donnees (CSV, Excel)

            Reponds toujours en francais, de maniere concise et utile.
            Si tu ne connais pas la reponse exacte, guide l'utilisateur vers la bonne section de l'application.
            """;

    // Simple in-memory session storage (limited to 10 messages per session)
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    /**
     * Process a chat message and return an AI response.
     */
    public AiChatResponse chat(String sessionId, AiChatRequest request) {
        if (!aiConfig.isEnabled()) {
            return defaultChatResponse(request);
        }

        // Get or create conversation history
        List<Map<String, String>> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add user message
        history.add(Map.of("role", "user", "content", request.getMessage()));

        // Limit to 10 messages
        while (history.size() > 10) {
            history.remove(0);
        }

        try {
            String response = callChatApi(history, request.getContext());

            // Add assistant response to history
            history.add(Map.of("role", "assistant", "content", response));

            // Extract suggested actions
            List<String> suggestions = extractSuggestions(response, request.getContext());

            return AiChatResponse.builder()
                    .response(response)
                    .suggestedActions(suggestions)
                    .build();

        } catch (Exception e) {
            log.error("Chat API error: {}", e.getMessage());
            return defaultChatResponse(request);
        }
    }

    /**
     * Clear conversation history for a session.
     */
    public void clearHistory(String sessionId) {
        sessionHistory.remove(sessionId);
    }

    private String callChatApi(List<Map<String, String>> history, String context) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String contextNote = context != null && !context.isBlank()
                ? " L'utilisateur se trouve actuellement sur la page: " + context + "."
                : "";

        String url;
        Map<String, Object> body;

        if ("openai".equalsIgnoreCase(aiConfig.getProvider())) {
            url = "https://api.openai.com/v1/chat/completions";
            headers.setBearerAuth(aiConfig.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT + contextNote));
            messages.addAll(history);

            body = Map.of(
                    "model", aiConfig.getModel(),
                    "messages", messages,
                    "max_tokens", aiConfig.getMaxTokens()
            );
        } else {
            // Anthropic
            url = "https://api.anthropic.com/v1/messages";
            headers.set("x-api-key", aiConfig.getApiKey());
            headers.set("anthropic-version", "2023-06-01");

            body = Map.of(
                    "model", aiConfig.getModel(),
                    "max_tokens", aiConfig.getMaxTokens(),
                    "system", SYSTEM_PROMPT + contextNote,
                    "messages", history
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

        return "Je n'ai pas pu traiter votre demande. Veuillez reessayer.";
    }

    private AiChatResponse defaultChatResponse(AiChatRequest request) {
        String msg = request.getMessage().toLowerCase();
        String response;
        List<String> suggestions = new ArrayList<>();

        if (msg.contains("eleve") || msg.contains("etudiant") || msg.contains("inscription")) {
            response = "Pour gerer les eleves, rendez-vous dans la section 'Gestion des Eleves'. " +
                    "Vous pouvez y ajouter, modifier, et consulter les fiches des eleves, " +
                    "gerer les inscriptions et les passages de classe.";
            suggestions.add("Aller a la page Eleves");
            suggestions.add("Importer des eleves via CSV");
        } else if (msg.contains("note") || msg.contains("bulletin") || msg.contains("examen")) {
            response = "Pour gerer les notes et bulletins, utilisez la section 'Pedagogie'. " +
                    "Vous pouvez saisir les notes par examen, generer les bulletins trimestriels, " +
                    "et consulter les moyennes par classe.";
            suggestions.add("Saisir des notes");
            suggestions.add("Generer les bulletins");
        } else if (msg.contains("paiement") || msg.contains("finance") || msg.contains("caisse")) {
            response = "La gestion financiere est accessible dans la section 'Finance'. " +
                    "Vous pouvez y enregistrer les paiements, suivre les impayes, " +
                    "gerer la caisse et generer des rapports financiers.";
            suggestions.add("Voir les paiements en retard");
            suggestions.add("Exporter les donnees financieres");
        } else if (msg.contains("absence") || msg.contains("retard") || msg.contains("presence")) {
            response = "Les absences et retards se gerent dans la section 'Absences'. " +
                    "Vous pouvez saisir les absences par classe, suivre les alertes, " +
                    "et generer des rapports de presence.";
            suggestions.add("Saisir les absences du jour");
            suggestions.add("Voir les eleves en alerte");
        } else if (msg.contains("emploi") || msg.contains("planning") || msg.contains("horaire")) {
            response = "L'emploi du temps est disponible dans la section dediee. " +
                    "Vous pouvez y creer des creneaux, affecter des salles " +
                    "et verifier les conflits de planning.";
            suggestions.add("Voir l'emploi du temps");
        } else if (msg.contains("import") || msg.contains("export") || msg.contains("csv") || msg.contains("excel")) {
            response = "Pour importer ou exporter des donnees, utilisez la section 'Import/Export'. " +
                    "Vous pouvez telecharger des templates CSV/Excel, importer des listes d'eleves " +
                    "ou d'enseignants, et exporter les donnees au format souhaite.";
            suggestions.add("Telecharger un template");
            suggestions.add("Importer des donnees");
        } else if (msg.contains("aide") || msg.contains("help") || msg.contains("comment")) {
            response = "Je suis la pour vous aider! Posez-moi des questions sur n'importe quelle " +
                    "fonctionnalite du systeme: gestion des eleves, notes, finance, absences, " +
                    "emploi du temps, import/export, etc.";
            suggestions.add("Comment ajouter un eleve ?");
            suggestions.add("Comment saisir les notes ?");
            suggestions.add("Comment exporter les donnees ?");
        } else {
            response = "Je peux vous aider avec la gestion des eleves, les notes et bulletins, " +
                    "la finance, les absences, l'emploi du temps, et l'import/export de donnees. " +
                    "N'hesitez pas a me poser une question plus specifique!";
            suggestions.add("Gestion des eleves");
            suggestions.add("Notes et bulletins");
            suggestions.add("Finance");
        }

        return AiChatResponse.builder()
                .response(response)
                .suggestedActions(suggestions)
                .build();
    }

    private List<String> extractSuggestions(String response, String context) {
        List<String> suggestions = new ArrayList<>();
        if (context != null) {
            switch (context.toLowerCase()) {
                case "students" -> {
                    suggestions.add("Voir les statistiques des eleves");
                    suggestions.add("Exporter la liste");
                }
                case "notes" -> {
                    suggestions.add("Generer les bulletins");
                    suggestions.add("Voir les moyennes");
                }
                case "finance" -> {
                    suggestions.add("Voir les impayes");
                    suggestions.add("Generer un rapport");
                }
                default -> suggestions.add("Poser une autre question");
            }
        }
        return suggestions;
    }
}
