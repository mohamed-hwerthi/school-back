package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BulletinTemplateDTO {
    private UUID id;
    private String nom;
    private String logoUrl;
    private String nomEcoleFr;
    private String nomEcoleAr;
    private String adresse;
    private String telephone;
    private String email;
    private String headerColor;
    private Boolean showLogo;
    private Boolean showPhotoEleve;
    private Boolean showAppreciation;
    private Boolean showRang;
    private String footerText;
    private Boolean actif;
}
