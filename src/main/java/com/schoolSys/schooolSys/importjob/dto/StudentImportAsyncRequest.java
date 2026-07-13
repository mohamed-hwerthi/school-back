package com.schoolSys.schooolSys.importjob.dto;

import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentImportAsyncRequest {
    /** Pre-validated rows from the wizard's preview step. */
    private List<StudentRequestDTO> rows;
    /** Conflict policy when a matricule/email already exists: SKIP (default) or UPDATE. */
    @Builder.Default
    private String strategy = "SKIP";
    /** Caller's identity (email or username), purely informational for the audit trail. */
    private String createdBy;
}
