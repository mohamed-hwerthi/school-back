package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.PenaliteRequestDTO;
import com.schoolSys.schooolSys.finance.dto.PenaliteResponseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PenaliteService {

    private final AnneeScolaireProvider anneeScolaireProvider;
    private final PenaliteRepository penaliteRepository;
    private final PenaliteMapper penaliteMapper;
    private final StudentRepository studentRepository;
    private final PaiementRepository paiementRepository;

    public List<PenaliteResponseDTO> findByAnneeScolaire(String anneeScolaire) {
        return penaliteMapper.toResponseDTOList(penaliteRepository.findByAnneeScolaire(anneeScolaire));
    }

    public List<PenaliteResponseDTO> findByStudentId(UUID studentId, String anneeScolaire) {
        return penaliteMapper.toResponseDTOList(
                penaliteRepository.findByStudentIdAndAnneeScolaire(studentId, anneeScolaire));
    }

    public PenaliteResponseDTO findById(UUID id) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penalite", id));
        return penaliteMapper.toResponseDTO(penalite);
    }

    @Transactional
    public PenaliteResponseDTO create(PenaliteRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

        Paiement paiement = null;
        if (dto.getPaiementId() != null) {
            paiement = paiementRepository.findById(dto.getPaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paiement", dto.getPaiementId()));
        }

        Penalite penalite = Penalite.builder()
                .student(student)
                .paiement(paiement)
                .montant(dto.getMontant())
                .motif(dto.getMotif())
                .dateApplication(dto.getDateApplication() != null ? dto.getDateApplication() : LocalDate.now())
                .anneeScolaire(anneeScolaireProvider.resolveAnneeScolaire(dto.getAnneeScolaire()))
                .payee(dto.getPayee() != null ? dto.getPayee() : false)
                .createdAt(LocalDateTime.now())
                .build();

        return penaliteMapper.toResponseDTO(penaliteRepository.save(penalite));
    }

    @Transactional
    public PenaliteResponseDTO update(UUID id, PenaliteRequestDTO dto) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penalite", id));

        if (dto.getStudentId() != null) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            penalite.setStudent(student);
        }

        if (dto.getPaiementId() != null) {
            Paiement paiement = paiementRepository.findById(dto.getPaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paiement", dto.getPaiementId()));
            penalite.setPaiement(paiement);
        }

        penalite.setMontant(dto.getMontant());
        penalite.setMotif(dto.getMotif());
        if (dto.getDateApplication() != null) penalite.setDateApplication(dto.getDateApplication());
        if (dto.getAnneeScolaire() != null) penalite.setAnneeScolaire(dto.getAnneeScolaire());
        if (dto.getPayee() != null) penalite.setPayee(dto.getPayee());

        return penaliteMapper.toResponseDTO(penaliteRepository.save(penalite));
    }

    @Transactional
    public void delete(UUID id) {
        if (!penaliteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Penalite", id);
        }
        penaliteRepository.deleteById(id);
    }

    @Transactional
    public PenaliteResponseDTO togglePayee(UUID id) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penalite", id));
        penalite.setPayee(!penalite.getPayee());
        return penaliteMapper.toResponseDTO(penaliteRepository.save(penalite));
    }
}
