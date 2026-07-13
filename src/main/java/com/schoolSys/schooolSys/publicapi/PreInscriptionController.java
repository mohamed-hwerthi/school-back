package com.schoolSys.schooolSys.publicapi;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PreInscriptionController {

    private final StudentRepository studentRepository;

    @PostMapping("/pre-inscription")
    public ResponseEntity<ApiResponse<String>> preInscription(@Valid @RequestBody PreInscriptionDTO dto) {
        Student student = Student.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .firstNameAr(dto.getFirstNameAr())
                .lastNameAr(dto.getLastNameAr())
                .sex(dto.getSex())
                .dateOfBirth(dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()
                        ? LocalDate.parse(dto.getDateOfBirth()) : null)
                .birthPlace(dto.getBirthPlace())
                .niveau(dto.getNiveau())
                .classe(dto.getClasse())
                .parentLastName(dto.getParentLastName())
                .parentFirstName(dto.getParentFirstName())
                .parentPhone(dto.getParentPhone())
                .parentEmail(dto.getParentEmail())
                .notes(dto.getNotes())
                .status("En attente")
                .isBlocked(false)
                .enrollmentDate(LocalDate.now())
                .build();

        studentRepository.save(student);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pré-inscription enregistrée avec succès"));
    }
}
