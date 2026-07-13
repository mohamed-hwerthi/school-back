package com.schoolSys.schooolSys.document;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentGenereRepository extends JpaRepository<DocumentGenere, UUID> {

    List<DocumentGenere> findAllByOrderByCreatedAtDesc();

    List<DocumentGenere> findByEleveIdOrderByCreatedAtDesc(UUID eleveId);

    List<DocumentGenere> findByTypeDocumentOrderByCreatedAtDesc(DocumentGenere.TypeDocument typeDocument);
}
