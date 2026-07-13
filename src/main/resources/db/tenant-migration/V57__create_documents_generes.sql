CREATE TABLE documents_generes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_document VARCHAR(50) NOT NULL,
    eleve_id UUID,
    file_name VARCHAR(300) NOT NULL,
    file_path VARCHAR(500),
    genere_par VARCHAR(200),
    annee_scolaire VARCHAR(20),
    trimestre INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_generes_eleve ON documents_generes(eleve_id);
CREATE INDEX idx_documents_generes_type ON documents_generes(type_document);
