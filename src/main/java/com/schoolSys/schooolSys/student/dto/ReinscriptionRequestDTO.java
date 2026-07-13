package com.schoolSys.schooolSys.student.dto;

import java.util.UUID;

import lombok.Data;

import java.util.List;

@Data
public class ReinscriptionRequestDTO {
    private List<UUID> studentIds;
    private String newClasse;
    private String newNiveau;
}
