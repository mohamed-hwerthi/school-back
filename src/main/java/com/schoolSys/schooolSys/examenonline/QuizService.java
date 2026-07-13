package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.examenonline.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final TentativeRepository tentativeRepository;
    private final CurrentUserContext currentUser;

    public List<QuizDTO> findAll(UUID classeId, String statut) {
        List<Quiz> quizzes;
        if (classeId != null) {
            quizzes = quizRepository.findByClasseIdOrderByCreatedAtDesc(classeId);
        } else if (statut != null) {
            quizzes = quizRepository.findByStatutOrderByCreatedAtDesc(statut);
        } else {
            quizzes = quizRepository.findAllByOrderByCreatedAtDesc();
        }
        // Row-level scoping: a teacher only sees quizzes in his classes.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            var scoped = currentUser.getScopedClasseIdsForTeacher();
            quizzes = quizzes.stream()
                    .filter(q -> q.getClasseId() != null && scoped.contains(q.getClasseId()))
                    .toList();
        }
        return quizzes.stream().map(this::toDTO).toList();
    }

    public QuizDTO findById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        assertCanAccessQuiz(quiz);
        return toDTO(quiz);
    }

    public QuizDetailDTO findDetailById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        assertCanAccessQuiz(quiz);

        List<Question> questions = questionRepository.findByQuizIdOrderByOrdreAsc(id);
        List<QuestionDTO> questionDTOs = questions.stream().map(this::toQuestionDTO).toList();

        return QuizDetailDTO.builder()
                .id(quiz.getId())
                .titre(quiz.getTitre())
                .description(quiz.getDescription())
                .moduleId(quiz.getModuleId())
                .classeId(quiz.getClasseId())
                .enseignantId(quiz.getEnseignantId())
                .dureeMinutes(quiz.getDureeMinutes())
                .noteTotale(quiz.getNoteTotale())
                .melangerQuestions(quiz.getMelangerQuestions())
                .melangerReponses(quiz.getMelangerReponses())
                .afficherResultats(quiz.getAfficherResultats())
                .tentativesMax(quiz.getTentativesMax())
                .dateDebut(quiz.getDateDebut())
                .dateFin(quiz.getDateFin())
                .statut(quiz.getStatut())
                .questions(questionDTOs)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    public List<QuizDTO> findByClasse(UUID classeId) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        return quizRepository.findByClasseIdOrderByCreatedAtDesc(classeId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public QuizDTO create(CreateQuizRequest request) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && request.getClasseId() != null
                && !currentUser.teacherTeachesClasse(request.getClasseId())) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        Quiz quiz = Quiz.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .moduleId(request.getModuleId())
                .classeId(request.getClasseId())
                .enseignantId(request.getEnseignantId())
                .dureeMinutes(request.getDureeMinutes())
                .noteTotale(request.getNoteTotale())
                .melangerQuestions(request.getMelangerQuestions())
                .melangerReponses(request.getMelangerReponses())
                .afficherResultats(request.getAfficherResultats())
                .tentativesMax(request.getTentativesMax())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .statut(request.getStatut())
                .build();
        return toDTO(quizRepository.save(quiz));
    }

    @Transactional
    public QuizDTO update(UUID id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        assertCanAccessQuiz(quiz);

        quiz.setTitre(request.getTitre());
        quiz.setDescription(request.getDescription());
        quiz.setModuleId(request.getModuleId());
        quiz.setClasseId(request.getClasseId());
        quiz.setEnseignantId(request.getEnseignantId());
        quiz.setDureeMinutes(request.getDureeMinutes());
        quiz.setNoteTotale(request.getNoteTotale());
        quiz.setMelangerQuestions(request.getMelangerQuestions());
        quiz.setMelangerReponses(request.getMelangerReponses());
        quiz.setAfficherResultats(request.getAfficherResultats());
        quiz.setTentativesMax(request.getTentativesMax());
        quiz.setDateDebut(request.getDateDebut());
        quiz.setDateFin(request.getDateFin());
        quiz.setStatut(request.getStatut());
        quiz.setUpdatedAt(LocalDateTime.now());

        return toDTO(quizRepository.save(quiz));
    }

    @Transactional
    public QuizDTO publish(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        assertCanAccessQuiz(quiz);
        quiz.setStatut("PUBLIE");
        quiz.setUpdatedAt(LocalDateTime.now());
        return toDTO(quizRepository.save(quiz));
    }

    @Transactional
    public void delete(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        assertCanAccessQuiz(quiz);
        quizRepository.deleteById(id);
    }

    /** A teacher may only read/manage quizzes that belong to one of his classes. */
    private void assertCanAccessQuiz(Quiz quiz) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && (quiz.getClasseId() == null
                    || !currentUser.teacherTeachesClasse(quiz.getClasseId()))) {
            throw new AccessDeniedException("Ce quiz n'est pas dans votre périmètre.");
        }
    }

    private QuizDTO toDTO(Quiz quiz) {
        long totalQuestions = questionRepository.countByQuizId(quiz.getId());
        long totalTentatives = tentativeRepository.countByQuizId(quiz.getId());

        return QuizDTO.builder()
                .id(quiz.getId())
                .titre(quiz.getTitre())
                .description(quiz.getDescription())
                .moduleId(quiz.getModuleId())
                .classeId(quiz.getClasseId())
                .enseignantId(quiz.getEnseignantId())
                .dureeMinutes(quiz.getDureeMinutes())
                .noteTotale(quiz.getNoteTotale())
                .melangerQuestions(quiz.getMelangerQuestions())
                .melangerReponses(quiz.getMelangerReponses())
                .afficherResultats(quiz.getAfficherResultats())
                .tentativesMax(quiz.getTentativesMax())
                .dateDebut(quiz.getDateDebut())
                .dateFin(quiz.getDateFin())
                .statut(quiz.getStatut())
                .totalQuestions(totalQuestions)
                .totalTentatives(totalTentatives)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuestionDTO toQuestionDTO(Question question) {
        List<ChoixReponseDTO> choixDTOs = question.getChoix().stream()
                .map(c -> ChoixReponseDTO.builder()
                        .id(c.getId())
                        .texte(c.getTexte())
                        .correct(c.getCorrect())
                        .ordre(c.getOrdre())
                        .build())
                .toList();

        return QuestionDTO.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .texte(question.getTexte())
                .typeQuestion(question.getTypeQuestion())
                .points(question.getPoints())
                .ordre(question.getOrdre())
                .explication(question.getExplication())
                .imageUrl(question.getImageUrl())
                .obligatoire(question.getObligatoire())
                .choix(choixDTOs)
                .build();
    }
}
