package com.schoolSys.schooolSys.parentnotif;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/parent-notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','DIRECTEUR','ENSEIGNANT')")
public class ParentNotificationController {

    private final ParentNotificationService service;
    private final NotificationLogRepository logRepo;

    @PostMapping("/note/{noteId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> notifyForNote(
            @PathVariable UUID noteId,
            @RequestBody SendRequest body) {
        Set<NotificationLog.Channel> channels = parseChannels(body.getChannels());
        return ResponseEntity.ok(ApiResponse.ok(
                service.sendForNote(noteId, channels, body.getTriggeredByUserId())));
    }

    @PostMapping("/examen/{examenId}")
    public ResponseEntity<ApiResponse<Integer>> notifyForExamen(
            @PathVariable UUID examenId,
            @RequestBody SendRequest body) {
        Set<NotificationLog.Channel> channels = parseChannels(body.getChannels());
        int count = service.sendForExamen(examenId, channels, body.getTriggeredByUserId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PostMapping("/manual/{studentId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> notifyManual(
            @PathVariable UUID studentId,
            @RequestBody ManualSendRequest body) {
        Set<NotificationLog.Channel> channels = parseChannels(body.getChannels());
        return ResponseEntity.ok(ApiResponse.ok(
                service.sendManualToParent(studentId, body.getMessage(), channels, body.getTriggeredByUserId())));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<NotificationLog>>> listLogs(
            @RequestParam(required = false) UUID recipientId,
            @RequestParam(required = false) ParentNotificationEvent eventType,
            @RequestParam(required = false) NotificationLog.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                logRepo.findFiltered(recipientId, eventType, status, pageable)));
    }

    private Set<NotificationLog.Channel> parseChannels(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Set.of(NotificationLog.Channel.SMS, NotificationLog.Channel.EMAIL);
        }
        Set<NotificationLog.Channel> out = new HashSet<>();
        for (String s : raw) out.add(NotificationLog.Channel.valueOf(s.toUpperCase()));
        return out;
    }

    @Data
    public static class SendRequest {
        private List<String> channels;
        private UUID triggeredByUserId;
    }

    @Data
    public static class ManualSendRequest {
        private String message;
        private List<String> channels;
        private UUID triggeredByUserId;
    }
}
