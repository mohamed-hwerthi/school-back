package com.schoolSys.schooolSys.messaging;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.messaging.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_MESSAGES')")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(
            @Valid @RequestBody MessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(messagingService.sendMessage(request)));
    }

    @GetMapping("/inbox/{recipientId}")
    @PreAuthorize("hasAuthority('READ_MESSAGES')")
    public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> getInbox(@PathVariable UUID recipientId) {
        return ResponseEntity.ok(ApiResponse.ok(messagingService.getInbox(recipientId)));
    }

    @GetMapping("/sent/{senderId}")
    @PreAuthorize("hasAuthority('READ_MESSAGES')")
    public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> getSent(@PathVariable UUID senderId) {
        return ResponseEntity.ok(ApiResponse.ok(messagingService.getSent(senderId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_MESSAGES')")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> getMessageById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(messagingService.getMessageById(id)));
    }

    @PutMapping("/{messageId}/read/{recipientId}")
    @PreAuthorize("hasAuthority('READ_MESSAGES')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID messageId,
            @PathVariable UUID recipientId) {
        messagingService.markAsRead(messageId, recipientId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{messageId}/recipient/{recipientId}")
    @PreAuthorize("hasAuthority('WRITE_MESSAGES')")
    public ResponseEntity<Void> deleteForRecipient(
            @PathVariable UUID messageId,
            @PathVariable UUID recipientId) {
        messagingService.deleteForRecipient(messageId, recipientId);
        return ResponseEntity.noContent().build();
    }
}
