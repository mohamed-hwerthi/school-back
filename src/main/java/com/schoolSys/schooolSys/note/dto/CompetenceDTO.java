package com.schoolSys.schooolSys.note.dto;

import java.util.UUID;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CompetenceDTO {
    private UUID id;
    private UUID moduleId;
    private String label;
    private String description;
}
