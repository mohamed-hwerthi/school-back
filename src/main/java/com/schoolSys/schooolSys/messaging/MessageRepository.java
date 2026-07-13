package com.schoolSys.schooolSys.messaging;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findBySenderIdOrderByCreatedAtDesc(UUID senderId);
}
