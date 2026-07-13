package com.schoolSys.schooolSys.notification.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnonceDTO {
    private UUID id;
    private String titre;
    private String contenu;
    private String type;
    private String destinataires;
    private UUID classeId;
    private UUID auteurId;
    private String auteurName;
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;
    private Boolean actif;
    private LocalDateTime createdAt;
}
