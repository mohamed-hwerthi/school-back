package com.schoolSys.schooolSys.vitrine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrineGalleryDTO {

    private UUID id;
    private String imageUrl;
    private String caption;
    private String category;
    private int displayOrder;
}
