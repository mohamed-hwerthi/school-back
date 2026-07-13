package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.examenonline.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TentativeService {

    private final TentativeRepository tentativeRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ChoixReponseRepository choixReponseRepository;
    private final ReponseEleveRepository reponseEleveRepository;

    public List<TentativeDTO> findByQuiz(UUID quizId) {
        return tentativeRepository.findByQuizIdOrderByDateDebutDesc(quizId)
                .stream().map(this::toDTO).toList();
    }

    public List<TentativeDTO> findByEleve(UUID eleveId) {
        return tentativeRepository.findByEleveIdOrderByDateDebutDesc(eleveId)
                .stream().map(this::toDTO).toList();
    }

    public TentativeDTO findById(UUID id) {
        Tentative tentative = tentativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative", id));
        return toDTOWithReponses(tentative);
    }

    @Transactional
    public TentativeDTO start(CreateTentativeRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.getQuizId()));

        // Check max attempts
        List<Tentative> existing = tentativeRepository.findByQuizIdAndEleveId(
                request.getQuizId(), request.getEleveId());
        if (existing.size() >= quiz.getTentativesMax()) {
            throw new IllegalStateException("Nombre maximum de tentatives atteint");
        }

        Tentative tentative = Tentative.builder()
                .quiz(quiz)
                .eleveId(request.getEleveId())
                .build();

        return toDTO(tentativeRepository.save(tentative));
    }

    @Transactional
    public TentativeDTO submitAnswers(SubmitReponseRequest request) {
        Tentative tentative = tentativeRepository.findById(request.getTentativeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tentative", request.getTentativeId()));

        if ("SOUMISE".equals(tentative.getStatut()) || "CORRIGEE".equals(tentative.getStatut())) {
            throw new IllegalStateException("Cette tentative a deja ete soumise");
        }

        tentative.setDateFin(LocalDateTime.now());
        tentative.setTempsPasseSecondes(
                (int) Duration.between(tentative.getDateDebut(), tentative.getDateFin()).getSeconds());

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalPoints = BigDecimal.ZERO;
        boolean allAutoCorrectible = true;

        // Clear existing responses
        tentative.getReponses().clear();

        if (request.getReponses() != null) {
            for (SubmitReponseRequest.ReponseItem item : request.getReponses()) {
                Question question = questionRepository.findById(item.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Question", item.getQuestionId()));

                totalPoints = totalPoints.add(question.getPoints());

                ReponseEleve reponse = ReponseEleve.builder()
                        .tentative(tentative)
                        .question(question)
                        .reponseTexte(item.getReponseTexte())
                        .build();

                // Auto-correct QCM and VRAI_FAUX
                if ("QCM".equals(question.getTypeQuestion()) || "VRAI_FAUX".equals(question.getTypeQuestion())) {
                    if (item.getChoixId() != null) {
                        ChoixReponse choix = choixReponseRepository.findById(item.getChoixId())
                                .orElse(null);
                        reponse.setChoix(choix);

                        if (choix != null && Boolean.TRUE.equals(choix.getCorrect())) {
                            reponse.setCorrect(true);
                            reponse.setPointsObtenus(question.getPoints());
                            totalScore = totalScore.add(question.getPoints());
                        } else {
                            reponse.setCorrect(false);
                            reponse.setPointsObtenus(BigDecimal.ZERO);
                        }
                    } else {
                        reponse.setCorrect(false);
                        reponse.setPointsObtenus(BigDecimal.ZERO);
                    }
                } else {
                    // TEXTE_LIBRE and REPONSE_COURTE need manual correction
                    allAutoCorrectible = false;
                }

                tentative.getReponses().add(reponse);
            }
        }

        if (allAutoCorrectible) {
            tentative.setStatut("CORRIGEE");
            tentative.setScore(totalScore);
            if (totalPoints.compareTo(BigDecimal.ZERO) > 0) {
                tentative.setScorePourcentage(
                        totalScore.multiply(BigDecimal.valueOf(100))
                                .divide(totalPoints, 2, RoundingMode.HALF_UP));
            }
        } else {
            tentative.setStatut("SOUMISE");
            tentative.setScore(totalScore);
        }

        return toDTOWithReponses(tentativeRepository.save(tentative));
    }

    public QuizStatsDTO getStats(UUID quizId) {
        quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        List<Tentative> tentatives = tentativeRepository.findByQuizIdOrderByDateDebutDesc(quizId);
        long totalTentatives = tentatives.size();

        BigDecimal moyenneScore = BigDecimal.ZERO;
        long scoredCount = tentatives.stream().filter(t -> t.getScore() != null).count();
        if (scoredCount > 0) {
            BigDecimal sum = tentatives.stream()
                    .filter(t -> t.getScore() != null)
                    .map(Tentative::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            moyenneScore = sum.divide(BigDecimal.valueOf(scoredCount), 2, RoundingMode.HALF_UP);
        }

        long reussite = tentatives.stream()
                .filter(t -> t.getScorePourcentage() != null && t.getScorePourcentage().compareTo(BigDecimal.valueOf(50)) >= 0)
                .count();
        double tauxReussite = totalTentatives > 0 ? (double) reussite / totalTentatives * 100 : 0;

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("0-25%", tentatives.stream().filter(t -> t.getScorePourcentage() != null && t.getScorePourcentage().compareTo(BigDecimal.valueOf(25)) <= 0).count());
        distribution.put("25-50%", tentatives.stream().filter(t -> t.getScorePourcentage() != null && t.getScorePourcentage().compareTo(BigDecimal.valueOf(25)) > 0 && t.getScorePourcentage().compareTo(BigDecimal.valueOf(50)) <= 0).count());
        distribution.put("50-75%", tentatives.stream().filter(t -> t.getScorePourcentage() != null && t.getScorePourcentage().compareTo(BigDecimal.valueOf(50)) > 0 && t.getScorePourcentage().compareTo(BigDecimal.valueOf(75)) <= 0).count());
        distribution.put("75-100%", tentatives.stream().filter(t -> t.getScorePourcentage() != null && t.getScorePourcentage().compareTo(BigDecimal.valueOf(75)) > 0).count());

        return QuizStatsDTO.builder()
                .totalTentatives(totalTentatives)
                .moyenneScore(moyenneScore)
                .tauxReussite(tauxReussite)
                .distributionNotes(distribution)
                .build();
    }

    private TentativeDTO toDTO(Tentative tentative) {
        return TentativeDTO.builder()
                .id(tentative.getId())
                .quizId(tentative.getQuiz().getId())
                .quizTitre(tentative.getQuiz().getTitre())
                .eleveId(tentative.getEleveId())
                .dateDebut(tentative.getDateDebut())
                .dateFin(tentative.getDateFin())
                .score(tentative.getScore())
                .scorePourcentage(tentative.getScorePourcentage())
                .statut(tentative.getStatut())
                .tempsPasseSecondes(tentative.getTempsPasseSecondes())
                .createdAt(tentative.getCreatedAt())
                .build();
    }

    private TentativeDTO toDTOWithReponses(Tentative tentative) {
        List<ReponseEleveDTO> reponseDTOs = tentative.getReponses().stream()
                .map(r -> ReponseEleveDTO.builder()
                        .id(r.getId())
                        .tentativeId(tentative.getId())
                        .questionId(r.getQuestion().getId())
                        .questionTexte(r.getQuestion().getTexte())
                        .choixId(r.getChoix() != null ? r.getChoix().getId() : null)
                        .reponseTexte(r.getReponseTexte())
                        .correct(r.getCorrect())
                        .pointsObtenus(r.getPointsObtenus())
                        .build())
                .toList();

        return TentativeDTO.builder()
                .id(tentative.getId())
                .quizId(tentative.getQuiz().getId())
                .quizTitre(tentative.getQuiz().getTitre())
                .eleveId(tentative.getEleveId())
                .dateDebut(tentative.getDateDebut())
                .dateFin(tentative.getDateFin())
                .score(tentative.getScore())
                .scorePourcentage(tentative.getScorePourcentage())
                .statut(tentative.getStatut())
                .tempsPasseSecondes(tentative.getTempsPasseSecondes())
                .reponses(reponseDTOs)
                .createdAt(tentative.getCreatedAt())
                .build();
    }
}
