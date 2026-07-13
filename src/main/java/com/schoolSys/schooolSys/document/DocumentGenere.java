package com.schoolSys.schooolSys.document;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "documents_generes")
@SQLDelete(sql = "UPDATE documents_generes SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentGenere {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "type_document", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    @Column(name = "eleve_id")
    private UUID eleveId;

    @Column(name = "file_name", nullable = false, length = 300)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "genere_par", length = 200)
    private String generePar;

    @Column(name = "annee_scolaire", length = 20)
    private String anneeScolaire;

    @Column
    private Integer trimestre;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TypeDocument {
        CERTIFICAT_SCOLARITE,
        CARTE_SCOLAIRE,
        ATTESTATION_REUSSITE,
        RELEVE_NOTES,
        RECU_PAIEMENT
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
