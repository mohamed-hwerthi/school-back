package com.schoolSys.schooolSys.messaging;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.messaging.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final CurrentUserContext currentUserContext;

    @Transactional
    public MessageResponseDTO sendMessage(MessageRequestDTO request) {
        // The sender is always the authenticated user, never trusted from the body.
        UUID senderId = currentUserContext.getUserId()
            .orElseThrow(() -> new AccessDeniedException("Utilisateur non authentifié."));
        Message message = Message.builder()
            .senderId(senderId)
            .subject(request.getSubject())
            .body(request.getBody())
            .type(request.getType() != null ? request.getType() : "MESSAGE")
            .build();

        List<MessageRecipient> recipients = request.getRecipientIds().stream()
            .map(recipientId -> MessageRecipient.builder()
                .message(message)
                .recipientId(recipientId)
                .build())
            .collect(Collectors.toList());

        message.getRecipients().addAll(recipients);
        return toDto(messageRepository.save(message));
    }

    public List<MessageResponseDTO> getInbox(UUID recipientId) {
        assertSelfOrAdmin(recipientId);
        return messageRecipientRepository
            .findByRecipientIdAndDeletedFalseOrderByMessageCreatedAtDesc(recipientId)
            .stream()
            .map(mr -> toDto(mr.getMessage()))
            .collect(Collectors.toList());
    }

    public List<MessageResponseDTO> getSent(UUID senderId) {
        assertSelfOrAdmin(senderId);
        return messageRepository.findBySenderIdOrderByCreatedAtDesc(senderId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public MessageResponseDTO getMessageById(UUID id) {
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Message", id));
        assertParticipant(message);
        return toDto(message);
    }

    @Transactional
    public void markAsRead(UUID messageId, UUID recipientId) {
        assertSelfOrAdmin(recipientId);
        MessageRecipient mr = messageRecipientRepository
            .findByMessageIdAndRecipientId(messageId, recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("MessageRecipient", messageId));
        if (mr.getReadAt() == null) {
            mr.setReadAt(LocalDateTime.now());
            messageRecipientRepository.save(mr);
        }
    }

    @Transactional
    public void deleteForRecipient(UUID messageId, UUID recipientId) {
        assertSelfOrAdmin(recipientId);
        MessageRecipient mr = messageRecipientRepository
            .findByMessageIdAndRecipientId(messageId, recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("MessageRecipient", messageId));
        mr.setDeleted(true);
        messageRecipientRepository.save(mr);
    }

    /**
     * Ensures the caller is acting on their own mailbox — unless they are an
     * administrator. Prevents reading or modifying another user's messages.
     */
    private void assertSelfOrAdmin(UUID targetUserId) {
        if (currentUserContext.hasUnrestrictedAccess()) {
            return;
        }
        UUID currentUserId = currentUserContext.getUserId().orElse(null);
        if (currentUserId == null || !currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Vous ne pouvez accéder qu'à vos propres messages.");
        }
    }

    /** Ensures the caller is the sender or one of the recipients of the message. */
    private void assertParticipant(Message message) {
        if (currentUserContext.hasUnrestrictedAccess()) {
            return;
        }
        UUID currentUserId = currentUserContext.getUserId().orElse(null);
        boolean participant = currentUserId != null
            && (currentUserId.equals(message.getSenderId())
                || message.getRecipients().stream()
                    .anyMatch(r -> currentUserId.equals(r.getRecipientId())));
        if (!participant) {
            throw new AccessDeniedException("Ce message n'est pas dans votre périmètre.");
        }
    }

    private MessageResponseDTO toDto(Message m) {
        List<MessageResponseDTO.RecipientDTO> recipients = m.getRecipients().stream()
            .map(mr -> MessageResponseDTO.RecipientDTO.builder()
                .id(mr.getId())
                .recipientId(mr.getRecipientId())
                .readAt(mr.getReadAt())
                .deleted(mr.getDeleted())
                .build())
            .collect(Collectors.toList());

        return MessageResponseDTO.builder()
            .id(m.getId())
            .senderId(m.getSenderId())
            .subject(m.getSubject())
            .body(m.getBody())
            .type(m.getType())
            .recipients(recipients)
            .createdAt(m.getCreatedAt())
            .build();
    }
}
