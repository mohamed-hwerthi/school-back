package com.schoolSys.schooolSys.parentnotif;

import lombok.Value;

/**
 * Centralise les templates de notification parent.
 * Phase 1 : en code (type-safe). Phase 2 : migrer vers une table notification_templates.
 */
public final class ParentNotificationTemplates {

    private ParentNotificationTemplates() {}

    @Value
    public static class Rendered {
        String subject;
        String body;
    }

    public static Rendered notePubliee(String childFirstName, String moduleName,
                                        String examenName, Double valeur, Integer trimestre) {
        String childName = childFirstName != null ? childFirstName : "votre enfant";
        String mod = moduleName != null ? moduleName : "—";
        String examen = examenName != null ? examenName : "—";
        String note = valeur != null ? String.format("%.2f", valeur) : "—";
        int t = trimestre != null ? trimestre : 1;

        String subject = String.format("Note publiée — %s (%s)", mod, examen);
        String body = String.format(
                "Bonjour, votre enfant %s a obtenu %s/20 en %s (%s — Trimestre %d). Cordialement, l'école.",
                childName, note, mod, examen, t);
        return new Rendered(subject, body);
    }

    public static Rendered manual(String customMessage) {
        return new Rendered("Message de l'école", customMessage);
    }

    public static Rendered absenceNonJustifiee(String childFirstName, String dateStr) {
        String childName = childFirstName != null ? childFirstName : "votre enfant";
        String subject = "Absence non justifiée";
        String body = String.format(
                "Bonjour, %s a été marqué absent le %s. Merci de justifier l'absence sous 48h.",
                childName, dateStr);
        return new Rendered(subject, body);
    }

    public static Rendered bulletinDisponible(String childFirstName, Integer trimestre) {
        String childName = childFirstName != null ? childFirstName : "votre enfant";
        int t = trimestre != null ? trimestre : 1;
        String subject = String.format("Bulletin du Trimestre %d disponible", t);
        String body = String.format(
                "Bonjour, le bulletin de %s pour le Trimestre %d est disponible. Connectez-vous au portail parent pour le consulter.",
                childName, t);
        return new Rendered(subject, body);
    }
}
