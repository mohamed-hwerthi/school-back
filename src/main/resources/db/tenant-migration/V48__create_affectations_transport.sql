CREATE TABLE affectations_transport (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL,
    circuit_id UUID NOT NULL REFERENCES circuits(id),
    arret_id UUID REFERENCES arrets(id),
    annee_scolaire VARCHAR(20) NOT NULL,
    actif BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_affectations_transport_eleve ON affectations_transport(eleve_id);
CREATE INDEX idx_affectations_transport_circuit ON affectations_transport(circuit_id);
