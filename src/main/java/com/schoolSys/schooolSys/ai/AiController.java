package com.schoolSys.schooolSys.ai;

import com.schoolSys.schooolSys.ai.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiChatService aiChatService;

    @PostMapping("/bulletin-comment")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<AiCommentResponse>> generateBulletinComment(
            @RequestBody AiCommentRequest request) {
        AiCommentResponse response = aiService.generateBulletinComment(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/performance-summary")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<String>> generatePerformanceSummary(
            @RequestBody AiPerformanceRequest request) {
        String summary = aiService.generatePerformanceSummary(request);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/detect-anomalies")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<AnomalyDTO>>> detectAnomalies(
            @RequestBody AiDetectAnomaliesRequest request) {
        List<AnomalyDTO> anomalies = aiService.detectAnomalies(request);
        return ResponseEntity.ok(ApiResponse.ok(anomalies));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @RequestBody AiChatRequest request,
            HttpSession session) {
        AiChatResponse response = aiChatService.chat(session.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/chat/history")
    public ResponseEntity<Void> clearChatHistory(HttpSession session) {
        aiChatService.clearHistory(session.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AiStatusResponse>> getStatus() {
        AiStatusResponse status = AiStatusResponse.builder()
                .enabled(true) // Always report as enabled - fallback handles disabled state
                .provider(aiConfig.getProvider())
                .model(aiConfig.getModel())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    private final AiConfig aiConfig;

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class AiStatusResponse {
        private boolean enabled;
        private String provider;
        private String model;
    }
}
