package com.schoolSys.schooolSys.teacher;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.teacher.dto.TeacherEvaluationDTO;
import com.schoolSys.schooolSys.teacher.dto.TeacherEvaluationStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherEvaluationService {

    private final TeacherEvaluationRepository evaluationRepository;
    private final TeacherRepository teacherRepository;

    public List<TeacherEvaluationDTO> findAll(UUID teacherId, String anneeScolaire) {
        List<TeacherEvaluation> evaluations;
        if (teacherId != null && anneeScolaire != null) {
            evaluations = evaluationRepository.findByTeacherIdAndAnneeScolaire(teacherId, anneeScolaire);
        } else if (teacherId != null) {
            evaluations = evaluationRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        } else if (anneeScolaire != null) {
            evaluations = evaluationRepository.findByAnneeScolaire(anneeScolaire);
        } else {
            evaluations = evaluationRepository.findAll();
        }
        return evaluations.stream().map(this::toDTO).toList();
    }

    public TeacherEvaluationDTO findById(UUID id) {
        TeacherEvaluation eval = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherEvaluation", id));
        return toDTO(eval);
    }

    @Transactional
    public TeacherEvaluationDTO create(TeacherEvaluationDTO dto) {
        TeacherEvaluation eval = TeacherEvaluation.builder()
                .teacherId(dto.getTeacherId())
                .evaluatorId(dto.getEvaluatorId())
                .evaluatorName(dto.getEvaluatorName())
                .anneeScolaire(dto.getAnneeScolaire())
                .trimestre(dto.getTrimestre())
                .ponctualite(dto.getPonctualite())
                .pedagogie(dto.getPedagogie())
                .discipline(dto.getDiscipline())
                .communication(dto.getCommunication())
                .implication(dto.getImplication())
                .commentaire(dto.getCommentaire())
                .build();
        return toDTO(evaluationRepository.save(eval));
    }

    @Transactional
    public TeacherEvaluationDTO update(UUID id, TeacherEvaluationDTO dto) {
        TeacherEvaluation eval = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherEvaluation", id));

        if (dto.getEvaluatorId() != null) eval.setEvaluatorId(dto.getEvaluatorId());
        if (dto.getEvaluatorName() != null) eval.setEvaluatorName(dto.getEvaluatorName());
        if (dto.getAnneeScolaire() != null) eval.setAnneeScolaire(dto.getAnneeScolaire());
        if (dto.getTrimestre() != null) eval.setTrimestre(dto.getTrimestre());
        if (dto.getPonctualite() != null) eval.setPonctualite(dto.getPonctualite());
        if (dto.getPedagogie() != null) eval.setPedagogie(dto.getPedagogie());
        if (dto.getDiscipline() != null) eval.setDiscipline(dto.getDiscipline());
        if (dto.getCommunication() != null) eval.setCommunication(dto.getCommunication());
        if (dto.getImplication() != null) eval.setImplication(dto.getImplication());
        if (dto.getCommentaire() != null) eval.setCommentaire(dto.getCommentaire());

        return toDTO(evaluationRepository.save(eval));
    }

    @Transactional
    public void delete(UUID id) {
        if (!evaluationRepository.existsById(id)) {
            throw new ResourceNotFoundException("TeacherEvaluation", id);
        }
        evaluationRepository.deleteById(id);
    }

    public TeacherEvaluationStatsDTO getStats(UUID teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        List<TeacherEvaluation> evaluations = evaluationRepository.findByTeacherId(teacherId);

        if (evaluations.isEmpty()) {
            return TeacherEvaluationStatsDTO.builder()
                    .teacherId(teacherId)
                    .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                    .avgPonctualite(0)
                    .avgPedagogie(0)
                    .avgDiscipline(0)
                    .avgCommunication(0)
                    .avgImplication(0)
                    .avgGlobale(0)
                    .totalEvaluations(0)
                    .build();
        }

        double avgPonctualite = evaluations.stream()
                .filter(e -> e.getPonctualite() != null)
                .mapToInt(TeacherEvaluation::getPonctualite)
                .average().orElse(0);

        double avgPedagogie = evaluations.stream()
                .filter(e -> e.getPedagogie() != null)
                .mapToInt(TeacherEvaluation::getPedagogie)
                .average().orElse(0);

        double avgDiscipline = evaluations.stream()
                .filter(e -> e.getDiscipline() != null)
                .mapToInt(TeacherEvaluation::getDiscipline)
                .average().orElse(0);

        double avgCommunication = evaluations.stream()
                .filter(e -> e.getCommunication() != null)
                .mapToInt(TeacherEvaluation::getCommunication)
                .average().orElse(0);

        double avgImplication = evaluations.stream()
                .filter(e -> e.getImplication() != null)
                .mapToInt(TeacherEvaluation::getImplication)
                .average().orElse(0);

        double avgGlobale = (avgPonctualite + avgPedagogie + avgDiscipline + avgCommunication + avgImplication) / 5.0;

        return TeacherEvaluationStatsDTO.builder()
                .teacherId(teacherId)
                .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                .avgPonctualite(Math.round(avgPonctualite * 10.0) / 10.0)
                .avgPedagogie(Math.round(avgPedagogie * 10.0) / 10.0)
                .avgDiscipline(Math.round(avgDiscipline * 10.0) / 10.0)
                .avgCommunication(Math.round(avgCommunication * 10.0) / 10.0)
                .avgImplication(Math.round(avgImplication * 10.0) / 10.0)
                .avgGlobale(Math.round(avgGlobale * 10.0) / 10.0)
                .totalEvaluations(evaluations.size())
                .build();
    }

    private TeacherEvaluationDTO toDTO(TeacherEvaluation eval) {
        String teacherName = teacherRepository.findById(eval.getTeacherId())
                .map(t -> t.getFirstName() + " " + t.getLastName())
                .orElse("Inconnu");

        return TeacherEvaluationDTO.builder()
                .id(eval.getId())
                .teacherId(eval.getTeacherId())
                .teacherName(teacherName)
                .evaluatorId(eval.getEvaluatorId())
                .evaluatorName(eval.getEvaluatorName())
                .anneeScolaire(eval.getAnneeScolaire())
                .trimestre(eval.getTrimestre())
                .ponctualite(eval.getPonctualite())
                .pedagogie(eval.getPedagogie())
                .discipline(eval.getDiscipline())
                .communication(eval.getCommunication())
                .implication(eval.getImplication())
                .noteGlobale(eval.getNoteGlobale())
                .commentaire(eval.getCommentaire())
                .createdAt(eval.getCreatedAt() != null ? eval.getCreatedAt().toString() : null)
                .build();
    }
}
