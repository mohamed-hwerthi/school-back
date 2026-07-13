package com.schoolSys.schooolSys.document.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.document.DocumentGenere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentGenerationRequest {

    private DocumentGenere.TypeDocument type;

    private UUID eleveId;

    private List<UUID> eleveIds;

    private String anneeScolaire;

    private Integer trimestre;
}
