package com.schoolSys.schooolSys.vitrine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrineSectionDTO {

    private UUID id;
    private UUID pageId;
    private String sectionType;
    private String title;
    private Map<String, Object> content;
    private int displayOrder;
    private boolean visible;
}
