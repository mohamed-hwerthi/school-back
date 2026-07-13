package com.schoolSys.schooolSys.messaging;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, UUID> {

    List<MessageRecipient> findByRecipientIdAndDeletedFalseOrderByMessageCreatedAtDesc(UUID recipientId);

    Optional<MessageRecipient> findByMessageIdAndRecipientId(UUID messageId, UUID recipientId);
}
