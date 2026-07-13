package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.BourseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BourseService {

    private final BourseRepository bourseRepository;
    private final StudentRepository studentRepository;

    public List<BourseDTO> findAll(String anneeScolaire) {
        List<Bourse> bourses;
        if (anneeScolaire != null && !anneeScolaire.isBlank()) {
            bourses = bourseRepository.findByAnneeScolaire(anneeScolaire);
        } else {
            bourses = bourseRepository.findAll();
        }
        return bourses.stream().map(this::toDTO).toList();
    }

    public List<BourseDTO> findByStudentId(UUID studentId) {
        return bourseRepository.findByStudentId(studentId).stream()
                .map(this::toDTO).toList();
    }

    public List<BourseDTO> findByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire) {
        return bourseRepository.findByStudentIdAndAnneeScolaire(studentId, anneeScolaire).stream()
                .map(this::toDTO).toList();
    }

    public BourseDTO findById(UUID id) {
        Bourse bourse = bourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bourse", id));
        return toDTO(bourse);
    }

    @Transactional
    public BourseDTO create(BourseDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

        Bourse bourse = Bourse.builder()
                .student(student)
                .type(dto.getType())
                .label(dto.getLabel())
                .montant(dto.getMontant())
                .pourcentage(dto.getPourcentage())
                .anneeScolaire(dto.getAnneeScolaire())
                .statut(dto.getStatut() != null ? dto.getStatut() : "ACTIVE")
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .motif(dto.getMotif())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toDTO(bourseRepository.save(bourse));
    }

    @Transactional
    public BourseDTO update(UUID id, BourseDTO dto) {
        Bourse bourse = bourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bourse", id));

        if (dto.getStudentId() != null && !dto.getStudentId().equals(bourse.getStudent().getId())) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            bourse.setStudent(student);
        }

        bourse.setType(dto.getType());
        bourse.setLabel(dto.getLabel());
        bourse.setMontant(dto.getMontant());
        bourse.setPourcentage(dto.getPourcentage());
        bourse.setAnneeScolaire(dto.getAnneeScolaire());
        bourse.setStatut(dto.getStatut() != null ? dto.getStatut() : "ACTIVE");
        bourse.setDateDebut(dto.getDateDebut());
        bourse.setDateFin(dto.getDateFin());
        bourse.setMotif(dto.getMotif());

        return toDTO(bourseRepository.save(bourse));
    }

    @Transactional
    public void delete(UUID id) {
        if (!bourseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bourse", id);
        }
        bourseRepository.deleteById(id);
    }

    private BourseDTO toDTO(Bourse bourse) {
        String studentName = bourse.getStudent().getFirstName() + " " + bourse.getStudent().getLastName();
        return BourseDTO.builder()
                .id(bourse.getId())
                .studentId(bourse.getStudent().getId())
                .studentName(studentName)
                .type(bourse.getType())
                .label(bourse.getLabel())
                .montant(bourse.getMontant())
                .pourcentage(bourse.getPourcentage())
                .anneeScolaire(bourse.getAnneeScolaire())
                .statut(bourse.getStatut())
                .dateDebut(bourse.getDateDebut())
                .dateFin(bourse.getDateFin())
                .motif(bourse.getMotif())
                .createdAt(bourse.getCreatedAt())
                .updatedAt(bourse.getUpdatedAt())
                .build();
    }
}
