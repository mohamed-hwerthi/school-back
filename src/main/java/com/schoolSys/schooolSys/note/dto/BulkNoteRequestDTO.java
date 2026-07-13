package com.schoolSys.schooolSys.note.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkNoteRequestDTO {

    @NotEmpty(message = "La liste des notes ne peut pas être vide")
    @Valid
    private List<NoteRequestDTO> notes;
}
