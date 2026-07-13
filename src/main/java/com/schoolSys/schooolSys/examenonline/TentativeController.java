package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.examenonline.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tentatives")
@RequiredArgsConstructor
public class TentativeController {

    private final TentativeService tentativeService;

    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<TentativeDTO>>> getByQuiz(@PathVariable UUID quizId) {
        return ResponseEntity.ok(ApiResponse.ok(tentativeService.findByQuiz(quizId)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<TentativeDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(tentativeService.findByEleve(eleveId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<TentativeDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(tentativeService.findById(id)));
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<TentativeDTO>> start(@Valid @RequestBody CreateTentativeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tentativeService.start(request)));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<TentativeDTO>> submit(@Valid @RequestBody SubmitReponseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tentativeService.submitAnswers(request)));
    }

    @GetMapping("/stats/{quizId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<QuizStatsDTO>> getStats(@PathVariable UUID quizId) {
        return ResponseEntity.ok(ApiResponse.ok(tentativeService.getStats(quizId)));
    }
}
