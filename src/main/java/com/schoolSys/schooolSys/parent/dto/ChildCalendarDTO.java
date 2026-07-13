package com.schoolSys.schooolSys.parent.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOB-FUNC-008 — événements calendrier d'un enfant entre deux dates :
 * devoirs, examens, événements école.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildCalendarDTO {

    private List<CalendarEvent> events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEvent {
        private UUID id;
        private String type;       // "DEVOIR" | "EXAMEN" | "EVENEMENT"
        private String titre;
        private String subtitle;
        private LocalDate date;
        private String couleur;
    }
}
