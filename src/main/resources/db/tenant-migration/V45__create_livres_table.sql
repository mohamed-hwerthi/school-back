CREATE TABLE livres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(300) NOT NULL,
    auteur VARCHAR(200),
    isbn VARCHAR(20),
    categorie VARCHAR(100),
    editeur VARCHAR(200),
    annee_publication INTEGER,
    description TEXT,
    nombre_exemplaires INTEGER DEFAULT 1,
    exemplaires_disponibles INTEGER DEFAULT 1,
    emplacement VARCHAR(100),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_livres_isbn ON livres(isbn);
CREATE INDEX idx_livres_categorie ON livres(categorie);
