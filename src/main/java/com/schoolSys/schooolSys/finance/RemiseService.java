package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.RemiseRequestDTO;
import com.schoolSys.schooolSys.finance.dto.RemiseResponseDTO;
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
public class RemiseService {

    private final AnneeScolaireProvider anneeScolaireProvider;
    private final RemiseRepository remiseRepository;
    private final RemiseMapper remiseMapper;
    private final StudentRepository studentRepository;
    private final TypeFraisRepository typeFraisRepository;

    public List<RemiseResponseDTO> findByAnneeScolaire(String anneeScolaire) {
        return remiseMapper.toResponseDTOList(remiseRepository.findByAnneeScolaire(anneeScolaire));
    }

    public List<RemiseResponseDTO> findByStudentId(UUID studentId, String anneeScolaire) {
        return remiseMapper.toResponseDTOList(
                remiseRepository.findByStudentIdAndAnneeScolaire(studentId, anneeScolaire));
    }

    public RemiseResponseDTO findById(UUID id) {
        Remise remise = remiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remise", id));
        return remiseMapper.toResponseDTO(remise);
    }

    @Transactional
    public RemiseResponseDTO create(RemiseRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

        TypeFrais typeFrais = null;
        if (dto.getTypeFraisId() != null) {
            typeFrais = typeFraisRepository.findById(dto.getTypeFraisId())
                    .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", dto.getTypeFraisId()));
        }

        Remise remise = Remise.builder()
                .student(student)
                .typeFrais(typeFrais)
                .type(dto.getType())
                .valeur(dto.getValeur())
                .estPourcentage(dto.getEstPourcentage() != null ? dto.getEstPourcentage() : false)
                .motif(dto.getMotif())
                .anneeScolaire(anneeScolaireProvider.resolveAnneeScolaire(dto.getAnneeScolaire()))
                .active(dto.getActive() != null ? dto.getActive() : true)
                .createdAt(LocalDateTime.now())
                .build();

        return remiseMapper.toResponseDTO(remiseRepository.save(remise));
    }

    @Transactional
    public RemiseResponseDTO update(UUID id, RemiseRequestDTO dto) {
        Remise remise = remiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remise", id));

        if (dto.getStudentId() != null) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            remise.setStudent(student);
        }

        if (dto.getTypeFraisId() != null) {
            TypeFrais typeFrais = typeFraisRepository.findById(dto.getTypeFraisId())
                    .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", dto.getTypeFraisId()));
            remise.setTypeFrais(typeFrais);
        }

        remise.setType(dto.getType());
        remise.setValeur(dto.getValeur());
        if (dto.getEstPourcentage() != null) remise.setEstPourcentage(dto.getEstPourcentage());
        remise.setMotif(dto.getMotif());
        if (dto.getAnneeScolaire() != null) remise.setAnneeScolaire(dto.getAnneeScolaire());
        if (dto.getActive() != null) remise.setActive(dto.getActive());

        return remiseMapper.toResponseDTO(remiseRepository.save(remise));
    }

    @Transactional
    public void delete(UUID id) {
        if (!remiseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Remise", id);
        }
        remiseRepository.deleteById(id);
    }
}
