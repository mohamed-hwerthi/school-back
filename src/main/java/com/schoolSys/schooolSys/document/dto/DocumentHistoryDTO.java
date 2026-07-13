package com.schoolSys.schooolSys.document.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.document.DocumentGenere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentHistoryDTO {

    private UUID id;

    private DocumentGenere.TypeDocument type;

    private String eleveName;

    private String fileName;

    private LocalDateTime dateGeneration;

    private String anneeScolaire;

    private Integer trimestre;
}
