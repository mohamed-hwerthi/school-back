package com.schoolSys.schooolSys.niveau.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NiveauResponseDTO {

    private UUID id;
    private String name;
    private List<String> sections;
}
