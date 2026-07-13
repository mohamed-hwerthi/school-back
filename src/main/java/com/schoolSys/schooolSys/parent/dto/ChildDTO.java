package com.schoolSys.schooolSys.parent.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String classe;
    private String niveau;
    private String matricule;
}
