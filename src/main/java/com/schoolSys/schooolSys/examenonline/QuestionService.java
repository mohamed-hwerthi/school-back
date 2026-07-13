package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.examenonline.dto.ChoixReponseDTO;
import com.schoolSys.schooolSys.examenonline.dto.CreateQuestionRequest;
import com.schoolSys.schooolSys.examenonline.dto.QuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final ChoixReponseRepository choixReponseRepository;

    public List<QuestionDTO> findByQuiz(UUID quizId) {
        return questionRepository.findByQuizIdOrderByOrdreAsc(quizId)
                .stream().map(this::toDTO).toList();
    }

    public QuestionDTO findById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        return toDTO(question);
    }

    @Transactional
    public QuestionDTO create(UUID quizId, CreateQuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        Question question = Question.builder()
                .quiz(quiz)
                .texte(request.getTexte())
                .typeQuestion(request.getTypeQuestion())
                .points(request.getPoints())
                .ordre(request.getOrdre())
                .explication(request.getExplication())
                .imageUrl(request.getImageUrl())
                .obligatoire(request.getObligatoire())
                .choix(new ArrayList<>())
                .build();

        question = questionRepository.save(question);

        if (request.getChoix() != null && !request.getChoix().isEmpty()) {
            for (ChoixReponseDTO choixDTO : request.getChoix()) {
                ChoixReponse choix = ChoixReponse.builder()
                        .question(question)
                        .texte(choixDTO.getTexte())
                        .correct(choixDTO.getCorrect())
                        .ordre(choixDTO.getOrdre())
                        .build();
                question.getChoix().add(choix);
            }
            question = questionRepository.save(question);
        }

        return toDTO(question);
    }

    @Transactional
    public QuestionDTO update(UUID id, CreateQuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));

        question.setTexte(request.getTexte());
        question.setTypeQuestion(request.getTypeQuestion());
        question.setPoints(request.getPoints());
        question.setOrdre(request.getOrdre());
        question.setExplication(request.getExplication());
        question.setImageUrl(request.getImageUrl());
        question.setObligatoire(request.getObligatoire());

        // Replace choices
        question.getChoix().clear();
        if (request.getChoix() != null) {
            for (ChoixReponseDTO choixDTO : request.getChoix()) {
                ChoixReponse choix = ChoixReponse.builder()
                        .question(question)
                        .texte(choixDTO.getTexte())
                        .correct(choixDTO.getCorrect())
                        .ordre(choixDTO.getOrdre())
                        .build();
                question.getChoix().add(choix);
            }
        }

        return toDTO(questionRepository.save(question));
    }

    @Transactional
    public void delete(UUID id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", id);
        }
        questionRepository.deleteById(id);
    }

    @Transactional
    public List<QuestionDTO> reorder(UUID quizId, List<UUID> questionIds) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrdreAsc(quizId);

        for (int i = 0; i < questionIds.size(); i++) {
            UUID qId = questionIds.get(i);
            questions.stream()
                    .filter(q -> q.getId().equals(qId))
                    .findFirst()
                    .ifPresent(q -> q.setOrdre(questionIds.indexOf(qId) + 1));
        }

        questionRepository.saveAll(questions);
        return questionRepository.findByQuizIdOrderByOrdreAsc(quizId)
                .stream().map(this::toDTO).toList();
    }

    private QuestionDTO toDTO(Question question) {
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
