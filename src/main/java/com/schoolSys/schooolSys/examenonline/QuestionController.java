package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.examenonline.dto.CreateQuestionRequest;
import com.schoolSys.schooolSys.examenonline.dto.QuestionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/{quizId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getByQuiz(@PathVariable UUID quizId) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.findByQuiz(quizId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<QuestionDTO>> getById(@PathVariable UUID quizId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<QuestionDTO>> create(
            @PathVariable UUID quizId,
            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(questionService.create(quizId, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<QuestionDTO>> update(
            @PathVariable UUID quizId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> delete(@PathVariable UUID quizId, @PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> reorder(
            @PathVariable UUID quizId,
            @RequestBody List<UUID> questionIds) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.reorder(quizId, questionIds)));
    }
}
